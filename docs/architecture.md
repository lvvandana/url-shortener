# Architecture Overview

This document describes the high-level architecture and control flow for the
URL shortener prototype contained in this repository.

Packages
- `com.vv.urlshortener.shortlink.api` — Spring MVC controllers and REST
  endpoints.
- `com.vv.urlshortener.shortlink.api.dto` — API request/response records.
- `com.vv.urlshortener.shortlink.application` — Service layer (business logic).
- `com.vv.urlshortener.shortlink.domain` — Domain utilities and exceptions
  (e.g. `ShortCodeGenerator`, validation exceptions).
- `com.vv.urlshortener.shortlink.persistence` — JPA entities and
  Spring Data repositories.
- `com.vv.urlshortener.common.error` — Global exception handler and API
  error response model.

Control flow (create a short link)
1. Client sends POST `/api/v1/short-links` with `originalUrl` and optional
   `customAlias` and `expiresAt`.
2. Controller maps request to DTO and calls `ShortLinkService.create(...)`.
3. `ShortLinkService` validates the destination URL via
   `DestinationUrlValidator` and checks optional custom alias availability.
4. If alias is not provided, `ShortCodeGenerator` (SecureRandom + readable
   alphabet) generates a candidate code; service retries on collision up to
   a fixed number of times, then persists using the repository.
5. Service returns a DTO with created information.

Control flow (redirect)
1. Client requests GET `/{code}`.
2. `RedirectController` delegates to `RedirectLookupService` to resolve the
   code to a `ShortLinkEntity` and to validate `expiresAt` (if present).
3. If the link is active, `AnalyticsService` records a click event and the
   controller returns `302 Found` with a `Location` header.
4. If the link is expired, the service throws `ShortLinkExpiredException`
   and the global handler returns HTTP 410; unknown codes return 404.

Persistence model
- `short_links` table: primary record for short links (id, code,
  original_url, created_at, updated_at, enabled, expires_at)
- `click_events` table: event-per-redirect (id, short_link_id, accessed_at)

Time handling
- All server-side time operations use a `java.time.Clock` injected into
  services, enabling deterministic tests and easier time manipulation.

Error handling and validation
- Input validation uses Jakarta Bean Validation on DTOs and a dedicated
  `DestinationUrlValidator` for URL-specific rules.
- Domain exceptions (alias collision, invalid URL, expired link) are mapped
  to consistent JSON `ApiError` responses by `GlobalExceptionHandler`.

Extensibility points
- Denormalized counters: `short_links.total_clicks` could be added later
  for efficient analytics reads.
- Asynchronous analytics: switch `AnalyticsService` to publish events for
  background processing when scale/latency requirements change.
