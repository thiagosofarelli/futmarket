# FutMarket — Football Player Token Market

A REST API that simulates a football player token market. Users can buy and sell player tokens whose value changes over time based on configurable valuation strategies.

[![Java CI with Maven](https://github.com/thiagosofarelli/futmarket/actions/workflows/ci.yml/badge.svg)](https://github.com/thiagosofarelli/futmarket/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=thiagosofarelli_futmarket&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=thiagosofarelli_futmarket)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=thiagosofarelli_futmarket&metric=coverage)](https://sonarcloud.io/summary/new_code?id=thiagosofarelli_futmarket)

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.5** (Web, Security, Data JPA, Validation, Docker Compose)
- **PostgreSQL 16** (via Docker)
- **H2** (in-memory, test scope only)
- **JWT** authentication (jjwt 0.12.6)
- **Maven** build tool
- **Lombok**, **ModelMapper**, **SpringDoc OpenAPI 3**

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| Docker + Docker Compose | any recent version |

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/thiagosofarelli/futmarket.git
cd futmarket
```

### 2. Configure environment variables

Copy the example file and fill in your values:

```bash
cp .env.properties.example .env.properties
```

Edit `.env.properties`:

```properties
# API key from football-data.org (required for player sync)
FOOTBALL_DATA_API_KEY=your_api_key_here

# JWT secret — any long random base64-encoded string
JWT_TOKEN=your_jwt_secret_here
```

> `.env.properties` is git-ignored. Never commit it.

### 3. Start the database

The app uses **Spring Boot Docker Compose** integration — PostgreSQL starts automatically when you run the application, as long as Docker is running.

Alternatively, you can start it manually:

```bash
docker compose up -d
```

This starts a PostgreSQL 16 container:

| Setting | Value |
|---------|-------|
| Host port | `5433` |
| Database | `futmarket` |
| User | `postgres` |
| Password | `root` |

### 4. Run the application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Documentation

Interactive Swagger UI is available once the app is running:

```
http://localhost:8080/swagger-ui.html
```

---

## Seeding Demo Data

To populate the database with demo users, players, orders, and quotes, call:

```bash
curl -X POST http://localhost:8080/admin/bootstrap/demo-data
```

This creates:
- A **superuser** (holds all tokens initially)
- Three regular users (`carla`, `marcos`, `lucia`) with password `demo1234`
- Several players with initial token prices and statistics
- Sample buy orders and quote history

The endpoint is **idempotent** — calling it more than once does not duplicate data.

---

## Authentication

All market endpoints require a JWT bearer token.

**Register a new user:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "myuser", "password": "mypassword"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "carla", "password": "demo1234"}'
```

Both return `{ "token": "<jwt>" }`. Pass it as a header on subsequent requests:

```
Authorization: Bearer <jwt>
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register a new user |
| POST | `/auth/login` | No | Login and get JWT |
| GET | `/players` | Yes | List players (filter by `league`, `team`, `playerPosition`; paginated) |
| GET | `/players/ranking` | Yes | Players ranked by token price |
| GET | `/players/{id}` | Yes | Single player detail |
| GET | `/players/{id}/quotes` | Yes | Quote history for a player |
| POST | `/quotes/recalculate` | Yes | Trigger quote recalculation (`strategy`: `GENERAL_PERFORMANCE` or `POSITION_WEIGHTED`) |
| GET | `/quotes/player/{id}` | Yes | Quotes for a player via quotes endpoint |
| POST | `/orders/buy` | Yes | Buy player tokens |
| POST | `/orders/sell` | Yes | Sell player tokens |
| GET | `/orders/user/{userId}` | Yes | Transaction history for a user |
| GET | `/users/{id}` | Yes | User profile |
| GET | `/users/{id}/portfolio` | Yes | User portfolio (via users endpoint) |
| GET | `/users/{id}/transactions` | Yes | User transaction history (via users endpoint) |
| POST | `/admin/bootstrap/demo-data` | No | Seed demo data |

**Buy/sell request body:**
```json
{ "playerId": 1, "quantity": 5 }
```

**Recalculate request body:**
```json
{ "strategy": "GENERAL_PERFORMANCE" }
```

---

## Running Tests

### Unit tests only

```bash
mvn test
```

Runs all tests except E2E. Uses an in-memory H2 database — no Docker required.

### Unit tests + E2E tests

```bash
mvn verify
```

E2E tests spin up the full Spring Boot application against H2 in-memory with the `e2e` profile. No external services needed.

---

## Project Structure

```
src/main/java/com/ar/edu/unq/futmarket/
├── controllers/        REST endpoints + DTOs + request/response classes
├── model/              JPA entities and enums
├── repositories/       Spring Data JPA interfaces
├── services/           Business logic
└── adapters/           External API clients (Football-Data.org, WhoScored)

src/test/java/com/ar/edu/unq/futmarket/
├── e2e/                End-to-end tests (run with mvn verify)
├── controllers/        Controller unit tests (MockMvc)
├── services/           Service unit tests
├── repositories/       Repository tests (H2)
└── model/              Domain model tests
```

---

## Valuation Strategies

Token prices are recalculated using one of two strategies:

**GENERAL_PERFORMANCE**
```
score = 0.25×goals + 0.15×assists + 0.10×shots + 0.10×keyPasses
      + 0.10×dribbles + 0.10×tackles + 0.20×rating
price = baseValue + (score × scaleFactor)
```

**POSITION_WEIGHTED** — same formula but with position-specific stat weights (forwards prioritize goals/shots, defenders prioritize tackles/interceptions, etc.)

Both are configurable via `application.properties`:
```properties
futmarket.valuation.base-value=1.0
futmarket.valuation.scale-factor=10.0
```
