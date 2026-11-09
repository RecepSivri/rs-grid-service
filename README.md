# rs-grid-service

Backend service of **rs-screen-builder**. A Spring Boot 3.3 / Java 21 REST API that

- **wraps the Keycloak Admin REST API** for authentication, user and group management, and
- stores the screen-builder domain data (projects, environments, pages, page fields, project types,
  technologies, data types) in **PostgreSQL via Spring Data JPA**.

## Tech stack

| Area | Technology |
|---|---|
| Language / build | Java 21, Maven 3.9+ |
| Framework | Spring Boot 3.3.4 (Web, Validation, Actuator) |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL (H2 in tests) |
| Security | Spring Security OAuth2 Resource Server (Keycloak JWT), realm-role based authorization |
| Identity | Keycloak 25 (`keycloak-admin-client` 25.0.6, RESTEasy 6.2) |
| API docs | springdoc-openapi 2.6.0 (Swagger UI) |
| Quality | JUnit 5, Mockito, Spring Security Test, JaCoCo (100% line + branch gate) |

## Architecture

```
                         ┌──────────────────────────────┐
Client (screen-builder)  │        rs-grid-service       │
  ── Bearer JWT ───────► │  Controller → Service        │
                         │                 │            │
                         │   ┌─────────────┴──────────┐ │
                         │   ▼                        ▼ │
                         │ Keycloak Admin API     JPA repositories
                         └───┬────────────────────────┬─┘
                             ▼                        ▼
                         Keycloak  ───────────►  PostgreSQL
                       (users, groups, tokens)   (projects, envs, pages, ...)
```

- **Identity data lives in Keycloak.** Users, groups, roles and tokens are never stored by this service;
  it talks to Keycloak only through the official Admin REST API and the token endpoint.
- **Domain data lives in PostgreSQL**, in the same server/database Keycloak uses by default. Tables are
  created/updated by Hibernate (`ddl-auto: update`) and pinned to the `public` schema so they never land
  in Keycloak's own schema.
- **Ownership is enforced in the service layer.** Projects, envs and pages carry a `userId`; edit/delete
  (and page-field operations) only succeed for the owner. A record that belongs to someone else is
  reported as *not found*, not *forbidden*, so IDs cannot be probed.

## Prerequisites

1. Java 21 and Maven 3.9+
2. PostgreSQL (default: `localhost:5432`, database `keycloak`)
3. Keycloak 25 (default: `http://localhost:8080`) with a realm (default `rs-grid`)

### One-time Keycloak setup

1. **Service-account client** used by this service for admin calls:
   *Clients → Create client* → Client ID `rs-grid-service`, **Client authentication: On**,
   **Service accounts roles: On**, and **Direct access grants: On** (`/auth/login` and `/auth/refresh`
   use this same client with the password / refresh-token grant). In the *Service accounts roles* tab assign
   `manage-users` (and `view-users`/`query-groups` or simply `realm-admin`) from the `realm-management`
   client. Copy the secret from the *Credentials* tab.
2. **Realm roles** used for authorization. Create the roles that the code checks (see the
   [role table](#authorization-rules) below): `admin`, `web`, `desktop`, and `desktop_user`.
3. Users created through `POST /api/v1/users` get the realm role that matches their `userType`
   (`admin`, `web` or `desktop`); the role must already exist in the realm or the call fails with 502.

## Configuration

Everything is configurable through environment variables (`src/main/resources/application.yml`):

| Environment variable | Description | Default |
|---|---|---|
| `KEYCLOAK_SERVER_URL` | Keycloak base URL | `http://localhost:8080` |
| `KEYCLOAK_REALM` | Realm that is managed | `rs-grid` |
| `KEYCLOAK_CLIENT_ID` | Confidential service-account client | `rs-grid-service` |
| `KEYCLOAK_CLIENT_SECRET` | Client secret | `CHANGE_ME` |
| `KEYCLOAK_ISSUER_URI` | Issuer used to validate incoming JWTs | `http://localhost:8080/realms/rs-grid` |
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/keycloak` |
| `DB_USERNAME` | DB user | `keycloak` |
| `DB_PASSWORD` | DB password | `keycloak` |
| `RS_GRID_SECURITY_ENABLED` | `false` disables JWT checks on all endpoints (**local development only**) | `true` |

Other defaults: server port `8081`, Keycloak connect/read timeout 5 s, connection pool size 10.

```bash
export KEYCLOAK_CLIENT_SECRET=<your-client-secret>
export DB_PASSWORD=<your-db-password>
```

> Never commit real secrets. The values above are local-development defaults only.

## Running

```bash
mvn clean spring-boot:run
```

Or build a jar:

```bash
mvn clean package
java -jar target/rs-grid-service.jar
```

| URL | Purpose |
|---|---|
| `http://localhost:8081/swagger-ui.html` | Interactive API docs (use **Authorize** to paste a JWT) |
| `http://localhost:8081/v3/api-docs` | OpenAPI 3 JSON |
| `http://localhost:8081/actuator/health` | Health check |
| `http://localhost:8081/actuator/info` | Build/info endpoint |

## Testing

```bash
mvn test        # unit + integration tests, JaCoCo report and coverage gate
mvn verify
```

- Controllers (`@WebMvcTest`), services (Mockito), configuration and exception handling are covered by
  ~25 test classes.
- `SecurityConfigEnabledIntegrationTest` / `SecurityConfigDisabledIntegrationTest` start the real
  security filter chain against in-memory **H2**, so no Keycloak or PostgreSQL is needed to run tests.
- JaCoCo fails the build if **line or branch coverage drops below 100%** (the application's `main` class
  is excluded). The HTML report is written to `target/site/jacoco/index.html`.

## API reference

All endpoints are under `/api/v1`. Unless noted, send `Authorization: Bearer <access_token>`.
`first` (default `0`) and `max` (default `50`) paginate list endpoints; `search` filters by name.

### Authentication — public

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/auth/login` | `{ username, password }` | Returns `access_token`, `refresh_token`, `expires_in`, `refresh_expires_in`, `token_type` |
| `POST` | `/auth/refresh` | `{ refreshToken }` | Exchanges a refresh token for a new access token |
| `POST` | `/auth/logout` | `{ refreshToken }` | Invalidates the session (`204`) |

### Users (Keycloak)

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Create a user. Body: `username`, `firstName`, `lastName`, `userType` (`admin` \| `web` \| `desktop`), optional `email`, `enabled`, `password`, `temporaryPassword` |
| `GET` | `/users/{userId}` | Get one user |
| `GET` | `/users?first=&max=&search=` | List users |
| `PUT` | `/users/{userId}` | Partial update (`email`, `firstName`, `lastName`, `enabled`, `password`, `temporaryPassword`); omitted fields are left unchanged |
| `DELETE` | `/users/{userId}` | Delete a user |

### Groups (Keycloak)

| Method | Path | Description |
|---|---|---|
| `GET` | `/groups?first=&max=&search=` | List realm groups (`id`, `name`, `path`, `subGroupCount`) |

### Projects

| Method | Path | Description |
|---|---|---|
| `POST` | `/projects` | Create. Body: `name`, `userId`, `technology`, `type` |
| `GET` | `/projects/{projectId}` | Get one |
| `GET` | `/projects?userId=&first=&max=&search=` | List the user's projects |
| `PUT` | `/projects/{projectId}?userId=` | Update (owner only) |
| `DELETE` | `/projects/{projectId}?userId=` | Delete (owner only) |

### Environments

| Method | Path | Description |
|---|---|---|
| `POST` | `/envs` | Create. Body: `name`, `url`, `userId` |
| `GET` | `/envs/{envId}` | Get one |
| `GET` | `/envs?userId=&first=&max=&search=` | List the user's envs |
| `PUT` | `/envs/{envId}?userId=` | Update (owner only) |
| `DELETE` | `/envs/{envId}?userId=` | Delete (owner only) |

### Pages

| Method | Path | Description |
|---|---|---|
| `POST` | `/pages` | Create. Body: `projectId`, `userId`, `name`, optional `getApi`, `getAllApi`, `addApi`, `updateApi`, `deleteApi`, `batchUpdateApi` |
| `GET` | `/pages/{pageId}` | Get one |
| `GET` | `/pages?projectId=&userId=&first=&max=&search=` | List a project's pages |
| `PUT` | `/pages/{pageId}?userId=` | Update (owner only) |
| `DELETE` | `/pages/{pageId}?userId=` | Delete (owner only) |

### Page fields

A page's fields are managed as one ordered list: there is no per-field CRUD.

| Method | Path | Description |
|---|---|---|
| `GET` | `/pages/{pageId}/fields?userId=` | Get the page's fields |
| `PUT` | `/pages/{pageId}/fields?userId=` | **Replace the whole list** (add / edit / remove in one call). Body: array of `{ dataField, caption, inputType, visible, readonly, required, dataTypeId, lookupTargetPageId, lookupValueField, lookupTemplate }`. Order of the array becomes the sort order |

### Reference data

| Resource | Endpoints | Notes |
|---|---|---|
| Project types | `POST/GET/GET {id}/PUT {id}/DELETE {id}` on `/project-types` | Body: `name` |
| Technologies | same on `/technologies` | Body: `name` |
| Data types | same on `/data-types` | Body: `name`, `kind` (`TEXT` \| `NUMBER` \| `DATE` \| `BOOLEAN` \| `LOOKUP`) |

List endpoints accept `first`, `max`, `search`.

### Example

```bash
# 1. Log in
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ahmet.yilmaz","password":"Passw0rd!"}' | jq -r .access_token)

# 2. Create a project
curl -X POST http://localhost:8081/api/v1/projects \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Orders","userId":"<user-id>","technology":"react","type":"web"}'

# 3. Replace a page's fields
curl -X PUT "http://localhost:8081/api/v1/pages/<pageId>/fields?userId=<user-id>" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '[{"dataField":"name","caption":"Name","inputType":"text","visible":true,"readonly":false,"required":true}]'
```

## Authorization rules

Authorization is based on the JWT's `realm_access.roles` claim (`KeycloakRealmRoleConverter` maps each
role to `ROLE_<role>`). Rules are declared in `SecurityConfig`:

| Endpoints | Required realm role |
|---|---|
| `/auth/**`, Swagger UI, `/v3/api-docs/**`, `/actuator/health`, `/actuator/info` | none (public) |
| `GET /users/**` | any valid JWT |
| `POST` / `PUT` / `DELETE /users/**` | `admin` |
| `/groups/**` | `admin` |
| `/project-types/**` (write and single read) | `admin` |
| `GET /project-types` (list) | `admin` or `desktop_user` |
| `/technologies/**`, `/data-types/**` (everything except list) | `admin` |
| `GET /technologies`, `GET /data-types` (list) | `admin` or `desktop_user` |
| `/projects/**`, `/envs/**`, `/pages/**` (incl. fields) | `admin` or `desktop_user`; plus owner check by `userId` in the service layer |

Forbidden requests return `403` with the standard JSON error body.

> **Role naming note:** `userType: desktop` assigns the realm role `desktop`, while the rules above check
> for `desktop_user`. Make sure desktop users actually carry the `desktop_user` role (for example by making
> `desktop` a composite role that includes `desktop_user`), otherwise they will receive `403` on project,
> env and page endpoints.

## Error format

```json
{ "status": 404, "error": "Not Found", "message": "…" }
```

Validation errors (`400`) additionally contain the per-field messages.

| Status | Meaning |
|---|---|
| `400` | Validation failure, missing parameter, unreadable body |
| `401` | Missing/invalid token, or wrong username/password on login |
| `403` | Authenticated but lacking the required role |
| `404` | Resource not found (also returned for resources owned by another user) |
| `409` | User already exists |
| `502` | Keycloak call failed (unreachable, missing role, permission problem, ...) |
| `500` | Unexpected error |

## Security notes

- CORS is intentionally **permissive** (`*` origin patterns) for local development; restrict it to the
  real frontend origins before deploying.
- CSRF is disabled and sessions are stateless because the API is token-based.
- `RS_GRID_SECURITY_ENABLED=false` removes all authentication; never use it outside local development.
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development; use a migration tool
  (Flyway/Liquibase) and `validate` in production.

## Project structure

```
src/main/java/com/rs/gridservice
├── RsGridServiceApplication.java
├── config        # SecurityConfig, KeycloakAdminClientConfig, KeycloakProperties,
│                 # KeycloakRealmRoleConverter, KeycloakObjectMapperResolver, OpenApiConfig
├── controller    # Auth, User, Group, Project, Env, Page, PageField,
│                 # ProjectType, Technology, DataType
├── dto           # request/response models, UserType
├── entity        # JPA entities (Project, Env, Page, PageField, ProjectType, Technology, DataType)
├── repository    # Spring Data repositories
├── service       # service interfaces
│   └── impl      # Keycloak*ServiceImpl (identity) and Jpa*ServiceImpl (domain data)
└── exception     # ApiError, GlobalExceptionHandler and domain exceptions
web/              # small Node.js page for manually testing login/refresh/logout and user creation
```

## Related

- **rs-screen-builder** — the TypeScript / Electron client that consumes this API.
- `web/` — dependency-free test UI (`cd web && npm start`, opens on `http://localhost:5500`).
