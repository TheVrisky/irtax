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
