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


## Active hierarchical legal document

### Endpoint
- `GET /api/v1/legal-documents/active`
- Returns the currently published and approved legal document in the standard `{ data, message, errors }` envelope.
- The Android client stores the complete document JSON in SQLite. On network/API failure it tries the last cached document, then falls back to the bundled local `TaxData`.

### Required hierarchy
`document → chapters → articles → sections → notes`

The transport DTO fields are:
- Document: `id`, `title`, optional `version`, `effective_at`, `published_at`, `chapters`.
- Chapter: `id`, `client_key`, `title`, `sort_order`, `articles`.
- Article: `id`, `number`, `content`, `sort_order`, `sections`, `notes`.
- Section: `id`, `title`, `content`, `sort_order`.
- Note: `id`, `label`, `content`, `sort_order`.

### Example payload
```json
{
  "data": {
    "id": "law-tax-invoice",
    "title": "قانون پایانه‌های فروشگاهی و سامانه مؤدیان",
    "version": "1404.04.08",
    "effective_at": "2025-06-29",
    "published_at": "2026-11-18",
    "chapters": [
      {
        "id": "chapter-1",
        "client_key": "fasl1",
        "title": "فصل اول - تعاریف و اصطلاحات",
        "sort_order": 1,
        "articles": [
          {
            "id": "article-1",
            "number": "ماده ۱",
            "content": "متن اصلی ماده",
            "sort_order": 1,
            "sections": [
              { "id": "section-1a", "title": "الف - سازمان", "content": "متن بند", "sort_order": 1 }
            ],
            "notes": [
              { "id": "note-1", "label": "تبصره ۱", "content": "متن تبصره", "sort_order": 1 }
            ]
          }
        ]
      }
    ]
  },
  "message": null,
  "errors": null
}
```

### Data integrity and user progress
- `client_key` must be non-empty and unique per document. To preserve existing reading history and bookmarks, use the existing chapter keys where applicable (currently `fasl1` through `fasl5`).
- Preserve the relative order of existing articles in each chapter. The current local reading-progress/bookmark store uses chapter key plus article position; inserting or reordering existing articles can otherwise point a saved position at the wrong article.
- Keep stable server IDs for documents, chapters, articles, sections and notes for future migrations and deep links.
- Do not silently publish OCR/import output. Imported PDF/TXT material must be draft, manually reviewed, versioned and only then published.
- Keep prior document versions and amendment metadata; the app displays only the active published version and retains the last successfully cached version offline.
- A legal-document response must not mix unrelated laws into this document tree. Each law/document is versioned independently.

### Implementation status
Android has the typed DTOs, hierarchical mapping, SQLite JSON cache and bank-screen loading with cache/local fallback. The Laravel endpoint and publishing/import workflow still need to be implemented on the server.


## Stable legal article identity and study-state migration

Android stores favorites, read-state, and the last-read reference using stable article IDs when available (`article:<id>`). Older installations used `chapterKey:index`; these values are migrated without clearing user data.

After a remote or cached legal document is applied, the app reconciles remaining legacy positional keys to stable IDs. Articles that do not yet have a stable ID retain their legacy key. Legal content synchronization must preserve the existing chapter client keys and existing article ordering until every legacy reference has been reconciled. Stable IDs are expected to survive editorial changes and numbering changes.

The app resolves stable IDs against the active document when showing favorites and the last-read article. Unknown IDs are retained in preferences rather than deleted, allowing a future document version to resolve them.


## Legal document version and amendment-history API

The active-document DTO includes immutable publication metadata:
- `documentId`, `version`
- `publicationDate`, `effectiveAt`, `expirationDate`, `amendmentDate`
- `status`, `checksum`, `publishedAt`

Android maps these values into `TaxContent` and tracks the active document version separately from user reading state. A cached legal document is stored by the composite `document_id + version`, rather than overwriting the previous version.

Additional history endpoints represented by Android:
- `GET /api/v1/legal-documents/{documentId}/versions`
- `GET /api/v1/legal-documents/{documentId}/amendments`
- `GET /api/v1/articles/{articleId}/history`

Version records are immutable after publication. Amendment records describe changes and point to the stable article identity. The API must not infer legal effective dates from publication dates.

## Study-state identity migration v2

Legacy `chapter:index` favorites/read markers are converted to `article:<uuid>` keys. For bundled content without a server UUID, Android derives a deterministic UUID from document ID, chapter key and article number. When remote content is applied, reconciliation maps bundled deterministic IDs to server article IDs when the chapter key/article number match. Unresolvable keys are retained.

The last-read record includes the active document version. This is diagnostic metadata and does not erase or reset progress when a new legal version is installed.
