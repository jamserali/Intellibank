# IntelliBank AI service

This service provides a deterministic, tool-backed assistant. `POST
/api/assistant/chat` accepts a natural-language banking question, routes
supported quantitative requests to the matching read-only Banking backend
`/api/v1` endpoint, and returns both a grounded explanation and the exact JSON
result. `/api/assistant/query` accepts an explicit tool name and arguments.
The caller's bearer token is forwarded to the backend so API role permissions
remain in force. Unsupported requests are explicitly identified; no
quantitative result is fabricated. See `skills/` for the assistant's policy
definitions, which are separate from executable tool implementations.

Example:

```json
{
  "tool": "get_client_profit_loss",
  "arguments": { "clientId": "C-123" }
}
```

Run with Java 21 and Maven:

```powershell
$env:BANKING_API_BASE_URL = "http://localhost:8080"
mvn spring-boot:run
```

## Backend and deployment limitations

The workspace contained no Banking backend or API specification. Default
routes in `application.yml` use conventional `/api/v1` paths and must be
checked against the real backend; non-templated routes can be overridden with
their `BANKING_API_*_PATH` environment variables. The implementation issues GET
requests only, passes through backend values without local calculations, and reports
backend errors instead of substituting values. Ranking calls default to
`limit=10`; comparison calls use `clientIds=id1,id2`. These query contracts
also require confirmation against the deployed API.
For the three client-scoped route templates, use the Spring relaxed environment
property names `BANKING_API_PATHS_CLIENTDETAILS`,
`BANKING_API_PATHS_CLIENTINVESTMENTS`, and `BANKING_API_PATHS_CLIENTPROFITLOSS`.

Ollama is optional for general financial terminology explanations only. Set
`OLLAMA_ENABLED=true`, `OLLAMA_BASE_URL`, and `OLLAMA_MODEL` to enable it. Numeric
questions remain deterministic and backend-grounded and do not use generated
financial figures.

Tests passed with Java 21 and Maven 3.9.9. The Dockerfile provides the same
Java 21/Maven build path for container deployments.
