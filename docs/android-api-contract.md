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
| POST | /api/v1/auth/logout | Revoke the current access token |
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
- Sanctum tokens are persisted locally using Android Keystore-backed encrypted preferences.
- The encrypted session file is excluded from cloud backup and device transfer.
- Logout clears the local session even when the server cannot be reached.
- The login screen, password reset, and account-state UI are not implemented yet.
- Do not send credentials or tokens to logs.
- PDF/TXT imports and legal-content approval remain server/admin responsibilities.


## Offline article cache
The Android client now has a SQLite cache for server-managed articles. The repository fetches paginated content, stores successful results, and falls back to cached content when the network or API is unavailable. This cache is separate from the existing static `TaxData` content and does not yet replace the current article screens.


## Exam delivery and grading contract

The Android exam client expects:
- `GET /api/v1/exams?page=1&per_page=100` returns only published/available exams in `data.items`.
- `GET /api/v1/exams/{id}` returns `data` with `id`, `title`, optional `description`, `question_count`, `duration_minutes`, and `questions`.
- Each question has `id`, `text`, and `options`; each option has `id` and `text`.
- Correct answers, grading keys, and explanations must not be included in the exam-detail response.
- `POST /api/v1/exams/{id}/submit` accepts `{ "answers": { "question-id": "option-id" } }`.
- The server validates exam publication/access, question membership, attempt limits and timing, then grades answers server-side. It returns `correct_count`, `total_questions`, optional `score`, and optional `message` in `data`.
- A 401 response means the user must sign in before submitting. The client does not locally grade or fabricate a server result.
- The existing five-question local quiz remains available independently when the API is offline or unavailable. Its local score is stored separately and is not represented as a server-certified attempt.

Example exam detail payload:

```json
{
  "data": {
    "id": "exam-001",
    "title": "آزمون نمونه",
    "description": "آزمون مقدماتی",
    "question_count": 1,
    "duration_minutes": 10,
    "questions": [
      {
        "id": "question-001",
        "text": "متن سؤال",
        "options": [
          { "id": "option-a", "text": "گزینه اول" },
          { "id": "option-b", "text": "گزینه دوم" }
        ]
      }
    ]
  },
  "message": null,
  "errors": null
}
```

Example submission body:

```json
{
  "answers": {
    "question-001": "option-a"
  }
}
```

## Legal-content hierarchy contract (next integration slice)

The current local content model is hierarchical and must remain so:
- document / law
- chapters
- articles (number and main text)
- sections (lettered clauses / بندها)
- notes (تبصره‌ها and notes)

The current reader, bookmarks and reading progress identify content using chapter ID plus article index. The server model must provide stable IDs for laws, chapters and articles; client mapping must preserve this local navigation identity or migrate bookmarks/progress explicitly. A future law endpoint should return a versioned document tree, its publication/effective dates and amendment metadata. Do not flatten sections or notes into the article body. Do not replace the existing local TaxData until the remote tree has been validated and cached. PDF/TXT imports must remain draft until a human reviews and publishes the extracted legal text.
