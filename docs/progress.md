# Implementation checklist

This checklist records what exists in the workspace and what was actually validated. An implemented file is not treated as a verified runtime feature when its test/build could not run.

| Phase | Status | Notes |
|---|---|---|
| Project setup | Done | Backend, frontend, MCP, AI, docs, CI and Compose module structure created. |
| Database | Implemented; runtime unverified | Flyway schema and deterministic synthetic initialization are in the backend. PostgreSQL startup could not be run here. |
| Banking API | Implemented; tests passed | Client/investment/ledger CRUD, P&L, dashboard/analytics routes and JWT role authorization are present. Backend tests: 5 passed. |
| Angular application | Implemented; build verified | Login, dashboard, client directory, investment CRUD, revenue/expense CRUD, P&L, analytics and chat UI are present. `npm run build` passed after the Angular upgrade. |
| MCP tools | Implemented; tests passed | Fourteen read-only tools use typed API calls and configured analyst service credentials. MCP tests: 3 passed. |
| AI assistant | Implemented with bounded scope | Quantitative requests use deterministic API-backed routing and caller JWT forwarding. Optional Ollama is limited to terminology explanations; this is not LLM-selected MCP tool calling. |
| Testing | Partial | Backend tests: 5 passed; MCP tests: 3 passed; AI tests: 4 passed; Angular production build passed; production frontend audit reports 0 vulnerabilities. Full npm audit still reports development-tool advisories. Docker/Compose runtime validation is unavailable. |
| Deployment | Configuration written; runtime unverified | Compose includes PostgreSQL, backend, MCP, AI, frontend and optional Ollama. Docker is not installed in this environment. |

## Known scope limits

- Historical valuations are synthetic snapshots; sale-level transaction accounting, dated FX conversion, and the complete production audit/chat persistence model are not implemented.
- Client listing search and pagination are handled in the UI after retrieving the API list, not by a server-side page query.
- The current UI is narrower than the requested full administration/reporting suite, and some dashboards are summary tables rather than every requested chart.
- MCP tools are available as an independent MCP server; the AI HTTP chat path currently calls authorized banking APIs directly rather than acting as a remote MCP client.
- Ollama is optional and used for general terminology explanations only. Quantitative tool selection/routing is deterministic.
