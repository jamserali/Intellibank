# MCP tool catalog

The MCP server exposes read-only access to banking data through the REST API using the configured backend analyst account. It must not connect directly to the database, execute SQL, mutate holdings, submit transactions or invent financial figures. Use the MCP service's tool descriptions and typed argument schema as the operational contract; API failures must be surfaced rather than replaced with fabricated results.

| Tool | Purpose |
|---|---|
| `get_all_clients` | Retrieve clients |
| `get_client_details` | Retrieve one client's details |
| `get_client_investments` | Retrieve investments associated with a client |
| `get_client_profit_loss` | Retrieve client P&L metrics |
| `get_bank_profit_loss` | Retrieve bank-level P&L |
| `get_top_profitable_clients` | Retrieve backend-ranked top clients |
| `get_bottom_profitable_clients` | Retrieve backend-ranked bottom clients |
| `get_portfolio_summary` | Retrieve allocation and valuation metrics |
| `get_revenue_summary` | Retrieve recognized revenue analytics |
| `get_expense_summary` | Retrieve recognized expense analytics |
| `get_sector_performance` | Retrieve server-calculated sector performance |
| `get_investment_performance` | Retrieve investment return metrics |
| `compare_client_profitability` | Retrieve comparable client metrics |
| `get_profit_trends` | Retrieve historical profit series |

Quantitative tools require API-backed response values and attribution to the source tool. Check the server configuration for API base URL, timeout and authentication requirements.
