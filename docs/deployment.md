# Deployment record (Step 9)

| Item | Value |
|------|-------|
| Live base URL | https://ipt-tracker-team<NN>.onrender.com   <!-- replace with your real URL -->
| Swagger UI | https://ipt-tracker-team<NN>.onrender.com/swagger-ui.html
| Database | Neon PostgreSQL (project `ipt-tracker`) |
| Host | Render, Docker runtime, Free instance, branch `main`, auto-deploy on |

## Environment variables set on Render (values are NOT stored in git)
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `SPRING_PROFILES_ACTIVE=prod`
Health check path: `/actuator/health`

## Smoke test (fill in after deploying)
| Check | Request | Expected | Result / screenshot |
|-------|---------|----------|---------------------|
| Health | `GET /actuator/health` | `{"status":"UP"}` | docs/deploy-health.png |
| Login | `POST /api/v1/auth/login` (student1@ipt.test) | 200 + accessToken | docs/deploy-login.png |
| Secured call | `GET /api/v1/placements` with Bearer token | 200 + page | docs/deploy-placements.png |
| No token | `GET /api/v1/placements` | 401 JSON error shape | docs/deploy-401.png |

## Auto-deploy check
Pushed commit `<hash>` to `main` on `<date/time>`; Render started a new deploy automatically: yes / no.

## Notes
- Free Render instances sleep after 15 minutes idle (~1 minute to wake). Open the URL a minute before the defence.
- Neon compute suspends when idle, so the first query after a pause is slower.
