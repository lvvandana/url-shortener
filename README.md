# URL Shortener (Prototype)

Lightweight URL shortener prototype implemented with Java 21 and Spring Boot.

Key features implemented in this repository:
- Create short links (generated codes or optional custom aliases)
- Redirect short codes to original URLs
- Optional expiration for short links (returns HTTP 410 Gone when expired)
- Basic click analytics: records one event per successful redirect and
  exposes total clicks, last accessed time and clicks grouped by UTC day
- File-backed H2 database for local development and Flyway migrations

Important: this repository is a prototype and includes unit and
integration tests; the test suite must be run locally (Java 21 required).

Quickstart
----------

Prerequisites
- Java 21 JDK installed and available on `PATH`
- Maven (the included `./mvnw` wrapper will be used automatically)

Run the application locally (development, using file-backed H2):

```zsh
./mvnw spring-boot:run
```

By default the application uses a file-backed H2 database located at
`./data/urlshortener` (H2 will create files such as
`data/urlshortener.mv.db`). Flyway migrations are applied on startup.

Run tests
---------

Run the full test-suite locally (requires Java 21):

```zsh
./mvnw clean verify
```

If you run into failures, please paste the failing test output and
the maintainer will iterate on minimal fixes.

API quick reference
-------------------

POST /api/v1/short-links
- Request JSON: `originalUrl` (required), `customAlias` (optional),
  `expiresAt` (optional ISO-8601 string)
- Response: 201 Created with JSON containing `code`, `shortUrl`,
  `originalUrl`, `createdAt`, `expiresAt`

GET /{code}
- Redirects (302 Found) to the original URL for active links.
- 404 Not Found when code does not exist.
- 410 Gone when the link exists but is expired.

GET /api/v1/short-links/{code}/analytics
- Returns JSON with `code`, `totalClicks`, `lastAccessedAt`, and
  `clicksByDay` grouped by UTC date.

Database migrations
-------------------
- `src/main/resources/db/migration/V1__create_short_links.sql` — creates
  `short_links` table
- `V2__add_short_link_expiration.sql` — adds nullable `expires_at`
- `V3__create_click_events.sql` — creates `click_events` table for analytics

Notes and limitations
---------------------
- This is a development/prototype repository. The H2 file-backed database
  and Flyway migrations are suitable for local development only.
- No IP addresses, cookies, or user-agent strings are stored (privacy by
  design for this prototype).
- Analytics recording is synchronous and may add latency to redirects; a
  production system should consider asynchronous processing and denormalized
  counters for scale.
- Do not assume tests were executed in this environment — run
  `./mvnw clean verify` locally to validate the current state.

Where to look next
- Source code: `src/main/java/com/vv/urlshortener` (controllers, services,
  persistence and domain)
- Flyway migrations: `src/main/resources/db/migration`
- Integration tests: `src/test/java/com/vv/urlshortener/shortlink`

If you want, I can also add a `docker-compose` development setup and a
brief checklist for hardening this service for production.
