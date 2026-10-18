# IntelliBank AI frontend

Angular single-page banking dashboard. All business data is loaded from the `/api/v1` service; the UI does not seed financial figures or substitute mock records when requests fail.

## Run locally

```sh
npm ci --legacy-peer-deps
npm start
```

The app is served on `http://localhost:4200`. The development proxy forwards `/api/v1` to the backend on port 8080 and `/api/assistant` to the AI service on port 8091. Sign in with a backend demo account before loading banking data. The Docker image includes the same Nginx reverse-proxy routes.

## API routes used

- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/trends`
- `GET`, `POST /api/v1/clients`
- `PUT`, `DELETE /api/v1/clients/{id}`
- `GET /api/v1/investments`
- `POST /api/v1/investments` with `InvestmentRequest`
- `PUT`, `DELETE /api/v1/investments/{id}`
- `GET /api/v1/investments/client/{clientId}`
- `GET`, `POST /api/v1/revenues`; `PUT`, `DELETE /api/v1/revenues/{id}`
- `GET`, `POST /api/v1/expenses`; `PUT`, `DELETE /api/v1/expenses/{id}`
- `GET /api/v1/reports/bank/pnl`
- `GET /api/v1/analytics/top-clients`
- `GET /api/v1/analytics/bottom-clients`
- `GET /api/v1/analytics/portfolio`
- `POST /api/assistant/chat` with `{ "message": "..." }`

List responses can be arrays or common `items`, `data`, or `results` envelopes. Client search and pagination are applied in the UI to the authenticated client list returned by the API. The dashboard adapts the backend's camelCase summary (`clientCount`, `investmentCount`, `totalRevenue`, `totalExpenses`, `accountingProfit`, `investedAmount`, `currentPortfolioValue`) and trend fields (`month`, `revenue`, `expenses`, `accountingProfit`, `portfolioValue`).

Investment writes send `clientId`, `name`, `assetType`, `sector`, `investedAmount`, `currentValue`, `realizedGain`, and `purchaseDate`. The investment form loads clients to provide the required client UUID. Revenue and expense writes send `description`, `category`, `amount`, `date`, and an optional `clientId`; the Revenue & expenses view records, lists, filters, updates, deletes, and exports entries.

Production build: `npm run build`.
