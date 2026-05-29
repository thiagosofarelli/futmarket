# futmarket

API REST para simular un mercado de tokens de jugadores de fútbol. Permite crear usuarios, comprar y vender tokens, recalcular cotizaciones y consultar la documentación OpenAPI/Swagger.

## Stack

- Java 17
- Spring Boot 4.0.5
- Maven
- Spring Data JPA
- PostgreSQL 16
- H2 para tests
- Spring Security
- SpringDoc OpenAPI / Swagger UI

## Requisitos

- JDK 17
- Maven Wrapper (`mvnw.cmd` en Windows)
- Docker y Docker Compose si querés levantar la base local

## Cómo ejecutar

### Levantar la app

```bash
./mvnw.cmd spring-boot:run
```

### Levantar con perfil de tests

```bash
./mvnw.cmd -DskipTests -Dspring-boot.run.profiles=test spring-boot:run
```

### Ejecutar tests y verificación completa

```bash
./mvnw.cmd verify
```

## Accesos útiles

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Base de datos

Por defecto la app usa PostgreSQL con Docker Compose. En tests se usa H2.

- PostgreSQL host: `localhost:5433`
- Base: `futmarket`
- Usuario: `postgres`
- Password: `root`

## CI y calidad

El repositorio incluye GitHub Actions para:

- build y tests unitarios
- coverage con JaCoCo
- análisis con SonarCloud
- dependencia automática de GitHub Advanced Security

SonarCloud usa `SONAR_TOKEN` como secret en GitHub Actions. El `projectKey` y la `organization` están definidos en el workflow.

## Endpoints principales

- `POST /admin/bootstrap/demo-data` para cargar datos demo
- `GET /players`
- `GET /players/{id}`
- `GET /players/{id}/quotes`
- `GET /players/ranking`
- `POST /quotes/recalculate`
- `POST /orders/buy`
- `POST /orders/sell`
- `GET /users/{id}/portfolio`
- `GET /users/{id}/transactions`

## Estado del proyecto

- Entidades JPA y repositorios implementados
- Servicio de bootstrap idempotente para datos demo
- E2E tests con perfil propio
- Swagger/OpenAPI disponible
- Cobertura con JaCoCo
- SonarCloud integrado en CI
