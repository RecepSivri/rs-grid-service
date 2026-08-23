# rs-grid-service

A Spring Boot 3.3 / Java 21 based user management service that **wraps the Keycloak Admin REST API**.

This service does not maintain its own database; users live entirely in Keycloak (and the PostgreSQL
it uses behind the scenes). `rs-grid-service` simply connects to the Keycloak Admin API using a
`client_credentials` grant and exposes `add / get / getAll / edit / delete` operations as a simplified
REST API for you.

## Architecture overview

```
Client  --(via JWT)-->  rs-grid-service  --(client_credentials)-->  Keycloak Admin API  --(JDBC)-->  PostgreSQL
```

- `rs-grid-service` authenticates requests to its own APIs (by default) using the JWT issued by Keycloak
  (`spring-boot-starter-oauth2-resource-server`).
- A separate **confidential client** (service account) is used for administrative requests sent to Keycloak
  (`org.keycloak:keycloak-admin-client`).
- Keycloak's own user/realm tables are already stored in your local PostgreSQL; this project does **not**
  touch those tables directly — it only uses Keycloak's official Admin REST API (this is the method
  Keycloak recommends and supports — direct access to the internal schema is not supported and breaks
  on upgrades).

## Prerequisites

1. Java 21, Maven 3.9+
2. A Keycloak instance running locally (default: `http://localhost:8080`), configured to use PostgreSQL
   as its database. To start Keycloak with Postgres (you can skip this step if Keycloak is already
   running), example environment variables:

   ```bash
   KC_DB=postgres
   KC_DB_URL=jdbc:postgresql://localhost:5432/keycloak
   KC_DB_USERNAME=keycloak
   KC_DB_PASSWORD=keycloak
   ```

   (These settings are passed to Keycloak's own `kc.sh` / docker startup command; rs-grid-service does
   not use them — they are provided for informational purposes only, since "have Keycloak use the local
   Postgres for its tables" is Keycloak's own configuration.)

## One-time setup required on the Keycloak side

For rs-grid-service to be able to manage users, you need to create a **confidential client** with the
service account enabled in the realm to be managed (assumed to be `rs-grid` below):

1. Keycloak Admin Console -> the relevant realm (e.g. `rs-grid`) -> **Clients** -> **Create client**
2. Client ID: `rs-grid-service`, Client authentication: **On** (confidential), you can turn off
   Standard flow / Direct access
3. **Service accounts roles**: mark it as **On**
4. After the client is created: go to the **Service accounts roles** tab -> **Assign role** ->
   switch the filter to the `realm-management` client -> assign at least the `manage-users` role
   (sufficient for creating/deleting/editing/listing users), or `realm-admin` for broader permissions.
5. Copy the **Client secret** from the **Credentials** tab.

## Configuration

All settings can be overridden with environment variables (see `src/main/resources/application.yml`):

| Environment variable | Description | Default |
|---|---|---|
| `KEYCLOAK_SERVER_URL` | Keycloak base URL | `http://localhost:8080` |
| `KEYCLOAK_REALM` | The realm in which users are managed | `rs-grid` |
| `KEYCLOAK_CLIENT_ID` | The confidential client id you created above | `rs-grid-service` |
| `KEYCLOAK_CLIENT_SECRET` | Client secret | `CHANGE_ME` |
| `KEYCLOAK_ISSUER_URI` | The issuer against which JWTs sent to rs-grid-service are validated | `http://localhost:8080/realms/rs-grid` |
| `RS_GRID_SECURITY_ENABLED` | If set to `false`, your own APIs are opened without a JWT (**local dev only**) | `true` |

The fastest way is to export `KEYCLOAK_CLIENT_SECRET` and `KEYCLOAK_REALM` (if needed) and run:

```bash
export KEYCLOAK_REALM=rs-grid
export KEYCLOAK_CLIENT_ID=rs-grid-service
export KEYCLOAK_CLIENT_SECRET=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/rs-grid
```

## Running

```bash
mvn clean spring-boot:run
```

> Note: In this session (the agent's isolated sandbox environment), this project could not be compiled
> or tested with `mvn compile` because there was no network access to Maven Central. The entire codebase
> was manually reviewed and written to match the Keycloak Admin Client APIs, but if you run into any
> minor compilation errors when running `mvn clean install` locally (especially around
> `keycloak-admin-client` / `resteasy` version compatibility), let me know and I'll fix them right away.

The service starts up on port `8081` by default.

## APIs

Base path: `/api/v1/users`. While `RS_GRID_SECURITY_ENABLED=true`, all endpoints require an
`Authorization: Bearer <token>` header (the token must be obtained from the same realm as
`KEYCLOAK_ISSUER_URI`).

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users` | Add a new user (add) |
| `GET` | `/api/v1/users/{userId}` | Get a single user (get) |
| `GET` | `/api/v1/users?first=0&max=50&search=` | List all users (getAll), pagination + search optional |
| `PUT` | `/api/v1/users/{userId}` | Update a user (edit) — fields not sent remain unchanged |
| `DELETE` | `/api/v1/users/{userId}` | Delete a user (delete) |

### Examples

Add a user:

```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
        "username": "ahmet.yilmaz",
        "email": "ahmet.yilmaz@example.com",
        "firstName": "Ahmet",
        "lastName": "Yilmaz",
        "enabled": true,
        "password": "Passw0rd!",
        "temporaryPassword": true
      }'
```

List all users:

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8081/api/v1/users?first=0&max=20"
```

Update a user:

```bash
curl -X PUT http://localhost:8081/api/v1/users/<userId> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"firstName": "Ahmet Can", "enabled": false}'
```

Delete a user:

```bash
curl -X DELETE http://localhost:8081/api/v1/users/<userId> -H "Authorization: Bearer $TOKEN"
```

## Project structure

```
src/main/java/com/rs/gridservice
├── RsGridServiceApplication.java
├── config
│   ├── KeycloakAdminClientConfig.java   # Keycloak Admin Client bean (client_credentials)
│   ├── KeycloakProperties.java          # rs-grid.keycloak.* config binding
│   └── SecurityConfig.java              # Filter chain protecting our own APIs with JWT
├── controller
│   └── UserController.java              # add/get/getAll/edit/delete REST endpoints
├── dto
│   ├── UserCreateRequest.java
│   ├── UserUpdateRequest.java
│   └── UserResponse.java
├── exception
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── KeycloakOperationException.java
│   ├── ResourceNotFoundException.java
│   └── UserAlreadyExistsException.java
└── service
    ├── UserService.java
    └── impl/KeycloakUserServiceImpl.java  # Core business logic wrapping the Keycloak Admin API
```

## What's next?

The top-priority **user services** (add/delete/edit/get/getAll) are complete. Topics we could discuss
as a next step:
- Group/role management endpoints (Keycloak realm roles / group assignment)
- Password reset / email verification trigger endpoints
- Integration tests (against a real Keycloak using Testcontainers)
- Rate limiting / caching (for frequently accessed users)
