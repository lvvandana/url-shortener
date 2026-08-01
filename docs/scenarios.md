# Scenarios and Requirements

This file summarizes the three scenarios used while implementing the
prototype: Greenfield, Brownfield and the Ambiguous analytics requirement.

Greenfield (initial feature set)
- Create short links from HTTP/HTTPS URLs.
- Generate a collision-resistant code using `SecureRandom` with a
  readable Base62-style alphabet.
- Support optional custom aliases (4–32 chars; letters, numbers, hyphen,
  underscore).
- Redirect `/{code}` to original URL with `302 Found`.
- Input validation with clear error responses.

Brownfield (incremental change)
- Add optional `expiresAt` to `short_links` via Flyway migration `V2`.
- New behaviour: redirects for expired links return HTTP 410 Gone.
- Existing links without `expiresAt` remain active and unchanged.

Ambiguous requirement — Analytics (engineer-approved interpretation)
- Record exactly one event for every successful redirect.
- Do not store IP addresses, cookies, or user-agent strings.
- Provide a read API that returns:
  - `totalClicks` for a given code
  - `lastAccessedAt` (instant of last click) or null when none
  - `clicksByDay` grouped by UTC date (map date -> count)
- Analytics are recorded synchronously for the prototype (simple, easier
  to reason about). The implementation uses a `click_events` table and the
  read endpoint aggregates by UTC day.

Acceptance criteria (summary)
- All APIs must return well-defined HTTP status codes for success and
  failure cases (400, 404, 409, 410 as appropriate).
- Database schema changes are additive and managed with Flyway V1..V3.
- Unit and integration tests cover create, redirect, expiration and
  analytics behaviour.
