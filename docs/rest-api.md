# REST API examples

The banking API uses `/api/v1`. Except for login and health, requests require a bearer token. The demo credentials are for local synthetic-data development only.

## Authenticate

```powershell
$login = Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/v1/auth/login `
  -ContentType 'application/json' `
  -Body '{"email":"admin@intellibank.local","password":"Admin123!"}'
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
```

## Read dashboard summary and rankings

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/v1/dashboard/summary -Headers $headers
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/analytics/top-clients?limit=5' -Headers $headers
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/reports/bank/pnl' -Headers $headers
```

## Create a client

```powershell
$client = @{
  name = 'Northstar Robotics'
  email = 'finance@northstar.example'
  riskProfile = 'MEDIUM'
  industry = 'Technology'
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/clients `
  -Headers $headers -ContentType 'application/json' -Body $client
```

## Ask the assistant

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8091/api/assistant/chat `
  -Headers $headers -ContentType 'application/json' `
  -Body '{"message":"Show the top 5 profitable clients"}'
```

For complete request/response types, validation rules, and endpoint details, see Swagger UI at `/swagger-ui/index.html` and the backend module README.
