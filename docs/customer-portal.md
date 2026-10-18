# Customer portal and authentication

The existing Angular and Spring Boot stack now includes `/`, `/signin`, `/signup`, `/admin/login`, `/dashboard`, `/admin`, recovery, privacy, terms and unauthorized pages. Customer registration uses the real backend, not browser-simulated authentication. All money and financial activity remain synthetic.

## Run locally

Start the backend with `mvn spring-boot:run` in `backend`, then `npm.cmd start` in `frontend`. Open http://localhost:4200. The dev proxy expects the backend on port 8080; adjust `frontend/proxy.conf.json` if using Compose's port 8081. Alternatively rebuild the existing Compose stack. Existing local admin credentials are `admin@intellibank.local` / `Admin123!`, unless overridden by the environment. Create a customer using Sign Up; there is no seeded customer password. Passwords require 12-72 characters, uppercase, lowercase, a number and a symbol (maximum 72 UTF-8 bytes).

## Database

Flyway applies V3 automatically on backend startup. Existing `user_accounts` stores UUID, unique normalized email, BCrypt password hash (cost 12), server-assigned role, enabled status and creation timestamp. New `customer_profiles` stores the account UUID as a primary/foreign key, full name, phone number and terms acceptance timestamp. Registration writes both records in one transaction. The migration extends the existing role constraint to permit CUSTOMER. Never edit an already-applied migration.

The default standalone H2 database is in memory and resets on restart. Use the existing Compose PostgreSQL volume for durable local records or configure DB_URL, DB_USERNAME, DB_PASSWORD and DB_DRIVER in the process environment. Spring Boot launched directly does not automatically load the root .env file. No credentials or secrets were read from or changed in .env for this work.

## Authorization

Registration always assigns CUSTOMER; submitted role fields cannot grant admin privileges. `/auth/me` verifies the token against enabled database accounts. Route guards verify the server session before rendering either dashboard. `/admin/customers` requires ADMIN. Customers cannot read staff financial APIs or their records. Existing staff roles retain their backend permissions; the new web admin portal is restricted to ADMIN. Session expiry redirects to sign in. Logout clears the browser tab session and returns home. Remember my email stores only an email, never a password or token, in localStorage.

After customer sign-in or registration, `/dashboard` shows the verified customer's profile, a clearly labeled illustrative balance, an empty transaction state, and account support links. It does not show bank-wide admin financial KPIs or imply access to a real ledger; real customer balances, transfers, and payments are not configured. The admin overview lists registered customers and presents live API-backed portfolio KPIs, bank profit trends, client profitability rankings, and asset-class allocation. Detailed analytics include sector profitability. Customer status updates and real payment monitoring are not implemented.

## Deployment baseline and remaining work

`SPRING_PROFILES_ACTIVE=production` requires explicit PostgreSQL credentials and JWT_SECRET, disables demo seeding and API documentation, and shortens the default token lifetime to 15 minutes. Supply a cryptographically random JWT_SECRET of at least 32 bytes through secret management. Admin accounts must be provisioned through a trusted operational process; public registration cannot create them. Activating the profile does not remove existing demo records: use a clean database or review existing accounts before release.

The Nginx container adds browser security headers and IP-based login/registration throttling (5 requests/minute, burst 10). This protection requires requests to pass through Nginx; direct backend and Angular dev-server access do not have that rate limit. Configure TLS, trusted proxy handling and distributed account-based throttling at the deployment edge before exposing the service.

This is a production-oriented baseline, not production-ready banking security. Tokens remain in sessionStorage and logout does not revoke a previously copied token; use a hardened HttpOnly-cookie session or identity provider with revocation and CSRF protection before release. Add MFA, email verification, audited recovery with expiring single-use tokens, durable audit logs, monitoring, backups/restore tests, privacy retention controls, dependency/security scanning and independent security review. Recovery currently provides an explicit support state and sends no email. Example contact addresses are placeholders. No real banking integrations or regulatory onboarding are configured.

Keep this existing application local until those requirements are addressed. A static frontend deployment alone cannot provide the Java API, PostgreSQL or working authentication.

## Validation

Run `mvn test` in backend and `npm.cmd run build` in frontend. Integration coverage includes customer registration, profile persistence, BCrypt verification, ignored role escalation, duplicate email rejection, invalid registration, anonymous access rejection and customer denial from staff/admin APIs. H2 migration checks do not replace verification against the target PostgreSQL server.
