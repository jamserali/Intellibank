# Client data skill

## Allowed tools
- `get_all_clients` — no arguments.
- `get_client_details` — requires `clientId`.
- `get_client_investments` — requires `clientId`.
- `get_client_profit_loss` — requires `clientId`.

Use only the exact identifier supplied by the caller. Never infer or invent
client identities, balances, investments, or profit/loss figures. If an ID is
missing or ambiguous, ask for the exact ID rather than making a backend call.
Present returned data as backend-reported and preserve its currency/period
labels; do not calculate unprovided metrics.
