# Bolsa de Jugadores — Project Context

## Language
Always respond in Spanish, regardless of the language used in prompts or code.

## Project Overview
Backend REST API simulating a football player token market. Users can buy and sell
player tokens whose value changes over time based on configurable valuation strategies.

## Stack
- Java 17
- Spring Boot 4.0.5
- Maven
- Spring Data JPA
- PostgreSQL 16 (via Docker Compose)
- H2 (test scope only)
- Lombok
- Jsoup (web scraping — pending)
- Spring Scheduler (periodic jobs — pending)

## Infrastructure
- App runs on port 8080
- PostgreSQL on port 5433 (host) → 5432 (container)
- DB name: `futmarket`, user: `postgres`, password: `root`
- Docker Compose manages both app and DB containers
- Spring Boot Docker Compose integration included (auto-starts compose on run)

## Package Structure
```
com.ar.edu.unq.futmarket
├── model
│   ├── enums          → OrderStatus, OrderType, PlayerPosition, ValuationStrategy
│   ├── Player.java
│   ├── Quote.java
│   ├── Order.java
│   ├── User.java
│   ├── Portfolio.java
│   └── Position.java
├── repositories       → Spring Data JPA interfaces
├── services           → business logic (stubs, pending implementation)
├── controllers        → REST endpoints (pending)
└── adapters           → external API clients and scrapers (pending)
```

## Domain Model

### Player
- `issuedTokens = 100` — `@Transient`, always 100, not persisted
- `availableTokens` — persisted, starts at 100, decremented on purchase and restored on sell
- `currentTokenPrice` — `BigDecimal`, starts at `1.0`
- `playerPosition` — `PlayerPosition` enum: `FORWARD`, `MIDFIELDER`, `DEFENDER`, `GOALKEEPER`
- Stats fields: `goals`, `assists`, `shots`, `keyPasses`, `dribbles`, `tackles`, `interceptions`, `rating`
- `externalId` — unique, used to link with external APIs
- `@Version` for optimistic locking

### User
- `username` — unique, not blank
- `balance` — `BigDecimal`, non-negative
- `superuser` — boolean; the superuser is the initial holder of all tokens
- `portfolio` — `@OneToOne`, cascaded, created automatically in constructor
- `@Version` for optimistic locking

### Portfolio
- Belongs to one `User` (`mappedBy = "portfolio"`)
- Has a list of `Position` (cascade ALL, orphanRemoval)
- `getCurrentValue()` — sum of all positions' current values
- `getProfitLoss()` — sum of all positions' P&L
- `registerPurchase(player, quantity)` — validates balance and available tokens, delegates to `Position`
- `registerSell(player, quantity)` — validates position exists, delegates to `Position`

### Position
- Unique constraint on `(portfolio_id, player_id)`
- `tokensAcquired` — total tokens currently held
- `averagePurchasePrice` — weighted average, recalculated on each purchase
- `registerPurchase(quantity, pricePerToken)` — updates average price, calls `player.subAvailableTokens()`
- `registerSell(quantity)` — decrements tokens, calls `player.addAvailableTokens()`; resets `averagePurchasePrice` to zero when position is empty
- `@Version` for optimistic locking

### Quote
- `@ManyToOne` → `Player`
- `currentTokenPrice` — price at the moment of calculation
- `strategy` — `ValuationStrategy` enum (`GENERAL_PERFORMANCE`, `POSITION_WEIGHTED`)
- `score` — numeric result of the valuation formula
- `calculatedAt` — set via `@PrePersist` if not provided

### Order
- Has both `buyer` and `seller` (`@ManyToOne` → `User`)
- `@ManyToOne` → `Player`
- `type` — `OrderType` enum: `BUY`, `SELL`
- `status` — `OrderStatus` enum: `PENDING`, `COMPLETED`, `FAILED`
- `tokenQuantity`, `pricePerToken`, `totalAmount`
- `createdAt` — set via `@PrePersist`; `completedAt` — nullable

## Domain Rules
- Each player has exactly 100 tokens issued (transient constant)
- Initial token value: 1 credit
- A superuser holds all tokens at time zero
- User purchases go against the superuser initially
- Buy/sell operations must be atomic and handle concurrency (optimistic locking via `@Version`)

## Valuation Strategies
Two strategies defined in `ValuationStrategy` enum; actual calculation logic is pending:

**GENERAL_PERFORMANCE:**
```
score = 0.25*goals + 0.15*assists + 0.10*shots + 0.10*keyPasses
      + 0.10*dribbles + 0.10*tackles + 0.20*rating
value = baseValue + (score * scaleFactor)
```

**POSITION_WEIGHTED:**
- FORWARD: prioritize goals and shots
- DEFENDER: prioritize tackles and interceptions
- MIDFIELDER: balanced mix
- GOALKEEPER: (to define)

Each `Quote` records which strategy version was used.

## Repositories
All extend `JpaRepository<Entity, Long>`:
- `PlayerRepository` — `findByLeague`, `findByTeam`, `findByPlayerPosition`, `findByLeagueAndTeam`, `findByLeagueAndPlayerPosition`
- `QuoteRepository` — `findByPlayerOrderByCalculatedAtDesc`, `findByPlayerIdOrderByCalculatedAtDesc`
- `OrderRepository` — find by buyer or seller id (desc by createdAt), find by player id
- `UserRepository`, `PortfolioRepository`, `PositionRepository` — base CRUD only

## External APIs (pending)
- **Football-Data.org**: official API for fixtures, lineups, results
  - API key stored in `application.properties` as `football.api.key`
- **WhoScored**: scraping with Jsoup for player performance metrics
  - Respect delays between requests to avoid blocks
  - Implement retries and user-agent spoofing

## API Endpoints (pending implementation)
| Method | Endpoint                  | Description                                     |
|--------|---------------------------|-------------------------------------------------|
| GET    | /players                  | List players (filter by league, team, position) |
| GET    | /players/:id              | Player detail                                   |
| GET    | /players/:id/quotes       | Quote history for a player                      |
| GET    | /players/ranking          | Ranking by active strategy                      |
| POST   | /quotes/recalculate       | Manually trigger quote recalculation            |
| POST   | /orders/buy               | Buy player tokens                               |
| POST   | /orders/sell              | Sell player tokens                              |
| GET    | /users/:id/portfolio      | User's current positions                        |
| GET    | /users/:id/transactions   | User's transaction history                      |

## Coding Conventions
- English for all code, comments, and commit messages
- Use `@Transactional` on all buy/sell service methods
- Wrap responses with `ResponseEntity<>`
- Global error handling with `@ControllerAdvice`
- Cache external API data locally — never call external APIs on every request
- Log all operations and quote recalculations for auditability
- Idempotent operations — no duplicates on retry

## Non-functional Requirements
- Atomic buy/sell transactions
- Concurrency handling via optimistic locking (`@Version` on Player, User, Portfolio, Position)
- Fallback to local data if external APIs are unavailable
- Weekly scheduled job for quote recalculation (Spring Scheduler)

## Architecture Layers
- **Controllers** — REST endpoints (pending)
- **Services** — business logic (stubs created, logic pending)
- **Repositories** — Spring Data JPA (implemented)
- **Adapters** — external API clients and scrapers (pending)

## Implementation Progress
- [x] Project structure and dependencies (pom.xml)
- [x] Docker Compose (PostgreSQL + app)
- [x] JPA entities: Player, User, Portfolio, Position, Quote, Order
- [x] Enums: PlayerPosition, ValuationStrategy, OrderType, OrderStatus
- [x] Repositories: Player, User, Portfolio, Position, Quote, Order
- [x] Domain logic in Portfolio and Position (buy/sell, average price, P&L)
- [ ] Service layer implementation (stubs exist)
- [ ] Controllers (REST endpoints)
- [ ] Valuation strategy calculation logic
- [ ] Weekly recalculation scheduler (Spring Scheduler)
- [ ] External adapters (Football-Data.org + WhoScored scraper)
- [ ] Error handling and logging (`@ControllerAdvice`)
