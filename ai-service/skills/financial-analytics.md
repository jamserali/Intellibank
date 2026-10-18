# Financial analytics skill

## Allowed tools
- `get_bank_profit_loss` — no arguments.
- `get_top_profitable_clients` / `get_bottom_profitable_clients` — optional
  `limit` (integer 1-100; default 10).
- `get_portfolio_summary`, `get_revenue_summary`, `get_expense_summary`,
  `get_sector_performance`, `get_investment_performance`, `get_profit_trends` —
  no arguments.
- `compare_client_profitability` — requires distinct `firstClientId` and
  `secondClientId`.

Rankings and comparisons must come directly from the backend. Do not sort,
aggregate, extrapolate, or fill missing values locally. State when the backend
does not return a requested metric or period. Do not claim success if the
backend request fails.
