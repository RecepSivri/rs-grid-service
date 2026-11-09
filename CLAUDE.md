# rs-grid-service

Postman benzeri API platformunun backend'i; `rs-screen-builder` frontend'ine hizmet verir.

## Stack
Spring Boot 3.3.4, Java 21, Maven. Spring Web, Validation, Data JPA + PostgreSQL (testlerde H2), OAuth2 Resource Server (Keycloak JWT), Keycloak Admin Client, springdoc OpenAPI, Actuator, Lombok, JaCoCo. Port 8081.

## Yapı (`com.rs.gridservice`)
- `config/` — Keycloak Admin Client, realm rol dönüştürücü, `SecurityConfig`, OpenAPI
- `controller/` — Auth, User, Group, Project, Env, Page, PageField, ProjectType, Technology, DataType
- `dto/` — `<Ad>CreateRequest`, `<Ad>UpdateRequest`, `<Ad>Response`
- `entity/` + `repository/` — `<Ad>Entity`, `<Ad>Repository`
- `service/` + `service/impl/`
- `exception/` — `GlobalExceptionHandler`, `ApiError`
- Kullanıcı/grup/rol Keycloak'ta; uygulama verisi PostgreSQL'de.

## API
Kök `/api/v1/`: `auth` (login/refresh/logout), `users`, `groups`, `projects`, `envs`, `pages`, page fields, `project-types`, `technologies`, `data-types`. Tam liste için OpenAPI (Swagger UI). README şu an yalnızca users'ı anlatıyor; güncellenmeli.

`POST /api/v1/proxy` — tarayıcı modunda hedef API'ye istek iletir (CORS'u aşmak için). `admin`/`desktop_user` rolü gerekir. Gövde: `{method, url, headers, body}`; yanıt: `{status, statusText, headers[], body, elapsedMs}` (hedefin 4xx/5xx'i de 200 zarfında döner; proxy'nin kendi reddi `ApiError` ile 400/403/413/502/504).
- Kod: `proxy/` (`TargetPolicy` SSRF kuralları, `OutboundProxyService`), ayarlar `rs-grid.proxy.*`.
- Link-local/metadata (169.254.0.0/16, fd00:ec2::254) her zaman engelli; loopback ve özel ağlar varsayılan engelli (`RS_GRID_PROXY_ALLOW_PRIVATE=true` veya `RS_GRID_PROXY_ALLOWED_HOSTS`). Yönlendirme takip edilmez; istek 2 MB, yanıt 5 MB, toplam süre 30 sn ile sınırlı; yalnız host loglanır.
- Bilinen sınır: DNS rebinding'e karşı JVM DNS önbelleğine güvenilir; üretimde konteyner çıkış ağı özel bloklara kapatılmalı.

## Ortam değişkenleri
`KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`, `KEYCLOAK_ISSUER_URI`, `RS_GRID_SECURITY_ENABLED` (yalnızca local dev'de false). Veritabanı bağlantısı `application.yml`'de; secret'lar koda yazılmaz.

## Docker ve yerel ortam
- `Dockerfile`: çok aşamalı (maven:3.9-eclipse-temurin-21 → eclipse-temurin:21-jre), port 8081.
- Compose tanımı `rs-screen-builder/docker-compose.yml`'de; gizli değerler oradaki `.env`'den gelir.
- `web/` — küçük Node test arayüzü.

## Komutlar
```bash
mvn clean spring-boot:run
mvn test
mvn verify    # JaCoCo dahil
```

## Kurallar
- Yeni modül: Entity → Repository → DTO → Service → Controller → test.
- Hata gövdesi her yerde `ApiError` şeklinde.
- Skill'ler: `sb-arge`, `sb-ui`, `sb-develop`, `sb-release` (`.claude/skills/`).
