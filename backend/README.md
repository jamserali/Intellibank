# IntelliBank AI backend

Spring Boot 3.4 / Java 21 REST service. Default local profile uses in-memory H2; PostgreSQL can be enabled by setting `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_DRIVER=org.postgresql.Driver`. Flyway manages the schema, and a deterministic initializer adds demonstration data when the database is empty.

## Run and validate

```sh
mvn spring-boot:run
mvn test
```

API docs: `/swagger-ui.html`; OpenAPI JSON: `/api-docs`; health: `/api/v1/health` (and Actuator `/actuator/health`).

## Authentication and authorization

All API routes except `POST /api/v1/auth/login` and health checks require a signed JWT in `Authorization: Bearer <accessToken>`. Login accepts `{"email":"...","password":"..."}` and returns `accessToken`, `tokenType`, `expiresIn` (seconds), and `role`. Tokens are signed with the configured HMAC secret and expire after one hour by default. Set `JWT_SECRET` to a private random value of at least 32 UTF-8 bytes and optionally `JWT_EXPIRATION_SECONDS` (60–86400); the checked-in default is for local development only and must never be used in production. Passwords are stored with BCrypt (cost 12); self-service registration is intentionally not exposed.

A clean local database is seeded with these demo accounts:

| Role | Email | Default demo password |
| --- | --- | --- |
| ADMIN | `admin@intellibank.local` | `Admin123!` |
| BANKER | `banker@intellibank.local` | `Banker123!` |
| ANALYST | `analyst@intellibank.local` | `Analyst123!` |
| VIEWER | `viewer@intellibank.local` | `Viewer123!` |

Override these before deployment with `DEMO_ADMIN_PASSWORD`, `DEMO_BANKER_PASSWORD`, `DEMO_ANALYST_PASSWORD`, and `DEMO_VIEWER_PASSWORD`. This initializer only creates absent accounts; it will not reset an existing account password. Example login:

```sh
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@intellibank.local","password":"Admin123!"}'
```

Role matrix: ADMIN has full access including deletion; BANKER can read and create/update clients, investments, revenues, and expenses; ANALYST and VIEWER have read-only access. Reads are authenticated for all roles. Invalid credentials or absent/invalid tokens receive 401; insufficient role receives 403. Swagger UI advertises the Bearer JWT scheme.

## API contract (`/api/v1`)

All identifiers are UUIDs. JSON dates use ISO-8601 `YYYY-MM-DD`. Financial amounts are decimal JSON numbers backed by `BigDecimal` and stored at scale 2. List endpoints return JSON arrays; missing records return 404 and invalid request bodies return 400 with a structured error.

`POST /auth/login` is the only non-health API endpoint that does not require a bearer token.

* `GET|POST /clients`; `GET|PUT|DELETE /clients/{id}`
* `GET|POST /investments`; `GET /investments/client/{clientId}`; `GET|PUT|DELETE /investments/{id}`. Investment fields: `clientId`, `name`, `assetType`, `sector`, `investedAmount`, `currentValue`, `realizedGain`, `purchaseDate`. The client-filtered list requires a valid client UUID and returns that client's investment array.
* `GET|POST /revenues`; `GET|PUT|DELETE /revenues/{id}`. Fields: `description`, `category`, `amount`, `date`, optional `clientId`.
* `GET|POST /expenses`; `GET|PUT|DELETE /expenses/{id}`. Fields: `description`, `category`, `amount`, `date`, optional `clientId`.
* `GET /clients/{id}/pnl` and `GET /reports/bank/pnl`, optionally `from` and `to` dates. P&L explicitly separates accounting profit (`revenue - expense`) from realized gains, unrealized gains (`current value - invested amount`) and economic return (`realized + unrealized`); investment returns are not included in accounting profit. Investment return fields are current position snapshots and are not period-prorated by the P&L date filter.
* `GET /dashboard/summary`; `GET /dashboard/trends?months=12`
* `GET /analytics/top-clients?limit=5`; `GET /analytics/bottom-clients?limit=5`; `GET /analytics/portfolio`; `GET /analytics/allocation`; `GET /analytics/sectors`

Client input fields: `name`, `email`, `riskProfile`, `industry`; responses also include `id` and `createdAt`. Revenue/expense client associations are optional; absent associations represent bank-level items. Ledger responses include `id`, `clientId`, `clientName`, `description`, `category`, `amount`, and `date`. Dashboard trends provide monthly revenue, expenses, accounting profit and portfolio valuation. Top/bottom clients are ranked server-side by economic return. Portfolio and sector analytics include exact invested/current values and returns.

When omitted, P&L `from` defaults to January 1 of the `to` year (or current year) and `to` defaults to today. Dashboard totals summarize all ledger entries; trends are the latest requested calendar months (1–60). Dashboard ROI is economic return divided by invested capital, expressed as a percentage and rounded to two decimal places. Clients with holdings have at least one investment position; profitable and loss-making counts are based on each client's combined realized and unrealized investment return. Asset allocation groups positions by asset type and includes the API-calculated invested value, current value and returns.
