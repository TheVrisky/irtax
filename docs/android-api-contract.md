# IRTAX Android API Contract (v1)

This document records the initial client-side contract. It is a development
agreement, not proof that the Laravel routes or response shapes already exist.

## Base URL
- HTTPS only.
- Configure at build time with Gradle property `IRTAX_API_BASE_URL`.
- Example: `./gradlew :app:assembleDebug -PIRTAX_API_BASE_URL=https://api.example.com/`
- The placeholder `https://api.example.invalid/` deliberately prevents accidental
  requests to an unset host.

## Headers
- `Accept: application/json`
- `X-Client: irtax-android`
- Authenticated requests: `Authorization: Bearer <Sanctum token>`

## Routes currently represented by the Android client
| Method | Path | Purpose |
|---|---|---|
| POST | /api/v1/auth/login | Sign in |
| GET | /api/v1/articles | Published articles, pagination and incremental sync |
| GET | /api/v1/articles/{id} | Article detail |
| GET | /api/v1/infographics | Published infographics |
| GET | /api/v1/exams | Available exams |
| GET | /api/v1/exams/{id} | Exam questions/details |
| POST | /api/v1/exams/{id}/submit | Submit answers |
| GET | /api/v1/conversations | User's private conversations |
| POST | /api/v1/conversations | Create a private conversation |
| GET | /api/v1/conversations/{id}/messages | Conversation messages |
| POST | /api/v1/conversations/{id}/messages | Send a message |

## Response envelope expected by this first client slice
```json
{
  "data": {},
  "message": null,
  "errors": null
}
```

Paged resources use `data.items` and optional `currentPage`, `lastPage`,
`perPage`, and `total` metadata.

## Important implementation notes
- JSON DTOs are transport models only. UI and local content models must not
  depend directly on API response classes.
- The Laravel backend must be aligned to these names, or the DTOs must be
  adjusted before enabling live calls.
- Token storage is intentionally not persisted in this first slice. A later
  authentication slice must add encrypted token storage, logout/revocation,
  expiry handling, and account-state UI.
- Do not send credentials or tokens to logs.
- PDF/TXT imports and legal-content approval remain server/admin responsibilities.
