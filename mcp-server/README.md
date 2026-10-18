# IntelliBank MCP server

Standalone Java 21 / Spring Boot 3.5 service exposing 14 synchronous, read-only
MCP tools. The tools return a structured `{tool, source, data}` result containing
the JSON payload received from the Banking backend; no balances, rankings, or
performance figures are computed or invented by this service.

## Run

```powershell
$env:BANKING_API_BASE_URL = "http://localhost:8080"
$env:BANKING_API_USERNAME = "analyst@intellibank.local"
$env:BANKING_API_PASSWORD = "Analyst123!"
mvn spring-boot:run
```

The MCP transport and endpoint are supplied by Spring AI's WebMVC MCP server
starter. The HTTP MCP endpoint is unauthenticated by default. Tool requests use the
configured analyst service account for backend reads. Keep the MCP endpoint on
a trusted network or put authentication/authorization in front of it before
exposing it outside localhost.

## Backend contract and limitations

The default route templates match the `/api/v1` backend contract in this
workspace. Routes can be overridden with corresponding `BANKING_API_*_PATH`
environment variables. The client performs GET requests only; backend response
bodies are passed through unchanged. Client comparison calls the client P&L
endpoint twice; ranking endpoints receive `limit`. A backend 4xx/5xx is surfaced with its
HTTP status; connection failures and timeouts are reported as gateway errors.
The client-scoped route defaults include `{clientId}` path variables. Override
those via Spring's relaxed property environment names
`BANKING_API_PATHS_CLIENTDETAILS`, `BANKING_API_PATHS_CLIENTINVESTMENTS`, and
`BANKING_API_PATHS_CLIENTPROFITLOSS` (rather than the generic override syntax,
which would need literal braces in a placeholder default).

Tool names: `get_all_clients`, `get_client_details`, `get_client_investments`,
`get_client_profit_loss`, `get_bank_profit_loss`, `get_top_profitable_clients`,
`get_bottom_profitable_clients`, `get_portfolio_summary`, `get_revenue_summary`,
`get_expense_summary`, `get_sector_performance`, `get_investment_performance`,
`compare_client_profitability`, and `get_profit_trends`.

Client IDs accept 1-64 ASCII letters, digits, dots, underscores, or hyphens.
Rank limits are 1-100. The service uses 3-second connect and 10-second read
timeouts by default; configure with `BANKING_API_CONNECT_TIMEOUT` and
`BANKING_API_READ_TIMEOUT`.

Spring AI MCP server starter version 1.1.8 was confirmed published in Maven
Central metadata on 2026-10-18. Tests passed with Java 21 and Maven 3.9.9.
