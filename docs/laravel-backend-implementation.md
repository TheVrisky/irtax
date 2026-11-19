# IRTAX Laravel Backend: implementation checklist

Status: planning/contract only. The Android client has not yet been connected to a deployed Laravel API. The VPS is managed by the project owner; no server commands were executed as part of this change.

## Backend architecture
- Laravel 13, MariaDB, Sanctum personal access tokens for Android.
- Separate Laravel session authentication for administrators.
- API prefix: `/api/v1`; all JSON responses use `{data, message, errors}`.
- Use Form Requests, API Resources, Policies, database transactions, rate limits and audit logging.
- Do not expose drafts or private support conversations to unauthorized users.

## Schema sequence
1. Review existing Laravel migrations and users table before altering them.
2. Categories, published articles, infographics.
3. Versioned legal documents: legal_documents, legal_chapters, legal_articles, legal_sections, legal_notes, legal_amendments.
4. Exams: exams, exam_questions, exam_options, exam_attempts, exam_attempt_answers.
5. Support: conversations, messages, attachments.
6. Source files and workflow: source_documents, content_imports, admin_logs.
7. Use stable UUIDs for legal content; preserve chapter `client_key` values `fasl1` through `fasl5`. Never delete or reorder published legal history.

## Required Android endpoints
- POST `/api/v1/auth/login`, POST `/api/v1/auth/logout`.
- GET `/api/v1/articles`, GET `/api/v1/articles/{id}`, GET `/api/v1/infographics`.
- GET `/api/v1/legal-documents/active`.
- GET `/api/v1/exams`, GET `/api/v1/exams/{id}`, POST `/api/v1/exams/{id}/submit`.
- GET/POST `/api/v1/conversations`; GET/POST `/api/v1/conversations/{id}/messages`.

## Exam security
Exam detail responses contain question text and option IDs/text only. Never serialize correct-answer flags. On submission, validate exam availability, attempt policy, question ownership and option ownership. Grade on the server inside a transaction. Reject client-supplied scores/correctness.

## Legal publishing workflow
1. Upload PDF/TXT privately; validate file type/size and record checksum.
2. Extract to a draft import. OCR/parser output is never published automatically.
3. Admin reviews source text against parsed chapters/articles/sections/notes.
4. Publish as a new immutable version, recording reviewer and effective/publication dates.
5. Activate the new version transactionally; retain all prior versions and amendment history.
6. Active endpoint returns one approved version; Android caches the complete tree and falls back to bundled TaxData.

## Admin panel
Build Blade + Tailwind RTL with session authentication. Required areas: dashboard, admins/roles, users, articles/categories/infographics, legal imports/review/version/publish, exam editor and attempt reports, support inbox/replies, audit log.

## Delivery gates
- Backend migrations and policies reviewed and tested on staging.
- API contract tests match Android DTOs and snake_case fields.
- Login/token revocation verified.
- No exam answer key leaks; server grading verified.
- Support ownership enforced.
- Legal PDF/TXT import reviewed and published; previous versions preserved.
- Android remote fetch/cache/offline fallback verified without losing bookmarks or reading positions.
- HTTPS, production error handling, backup restore and security review complete.

## Owner boundary
The project owner executes VPS commands and deploys Laravel. The Android changes are made in the GitHub repository. This checklist is not a claim that backend routes, migrations, admin UI, or import processing have already been deployed.


## Versioned legal-content schema (reviewed contract)

Recommended relational model and constraints:

### `legal_documents`
- `id`: UUID primary key
- `slug`: unique
- `title`, `document_type`, `official_number`, `description`
- `active_version_id`, `latest_version_id` (nullable UUID references)
- timestamps

### `legal_document_versions`
- `id`: UUID primary key
- `legal_document_id`: foreign key
- `version_number`: string; unique per document
- `publication_date`, `effective_date`, `expiration_date`, `amendment_date`
- `status`: draft/review/approved/published/archived
- `source_document_id`, `checksum`
- `published_at`, `published_by`, `reviewed_by`
- timestamps
- Unique index: (`legal_document_id`, `version_number`)
- Published versions are immutable; activating a version changes only the document pointer.

### `legal_chapters`
- `id`: UUID
- `legal_document_version_id`: foreign key
- `client_key`: e.g. fasl1 through fasl5
- `title`, `chapter_number`, `sort_order`
- Unique: (`legal_document_version_id`, `client_key`)

### `legal_articles`
- `id`: stable UUID for the legal article across versions
- `legal_document_version_id`, `legal_chapter_id`
- `article_number`, `title`, `content`, `summary`, `sort_order`
- `effective_date`, `expiration_date`
- Unique per version: (`legal_document_version_id`, `article_number`) only if the source law guarantees uniqueness; otherwise chapter-scoped uniqueness.
- Keep a stable article identity separate from version-specific article rows if the text/number can change. Recommended split: `legal_article_identities` (stable UUID) and `legal_article_versions` (versioned text/number/chapter).

### `legal_sections` and `legal_notes`
- UUID, versioned article foreign key, label/title, content, sort_order.
- Preserve بند, تبصره, footnote and other nested structures without flattening them into article content.

### `legal_amendments`
- UUID, document ID, stable article identity, source/amending document
- `amendment_type`, `previous_text`, `new_text`
- `publication_date`, `effective_date`, `source_reference`
- reviewer/author and timestamps

### `legal_sources`
- UUID, original filename, mime type, file size, private storage path, checksum, uploaded_by, review_status, timestamps.
- Store source files privately; expose downloads only to authorized admins.

### Migration safety
- Do not destructively alter the current users table.
- Build schema in additive migrations and review existing migrations first.
- Use transactions when publishing/activating versions.
- Keep the previous active version available for rollback.
- Article identity must not be based on article number or list position.
- A version's content checksum is computed from normalized content and metadata, not from a mutable timestamp.

This schema is the backend implementation specification. The GitHub repository currently contains Android code and documentation, not the Laravel application source. Actual Laravel migrations, Eloquent models, controllers and admin views must be committed in the Laravel backend repository or created on the owner-managed VPS; they are not claimed to be implemented here.
