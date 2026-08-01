# AI Assistance and Engineer Review

This repository was developed with AI-assisted code generation and human
engineer review. This document explains what was generated, review notes
and the expectations for maintainers.

What the AI produced
- Code artifacts generated during the exercise include domain utilities
  (short-code generator, URL validator), service layer, controllers,
  JPA entities, repositories, Flyway migrations (V1..V3) and tests.
- Several text artifacts (plans, architecture) were also produced.

Engineer responsibilities and review notes
- All AI-generated code must be reviewed by a knowledgeable engineer
  before merging into any shared branch.
- Pay particular attention to:
  - Input validation and escaping of user-supplied values
  - Time-zone correctness when aggregating analytics (this project uses
    UTC for grouping)
  - Database indexes and query performance (the prototype uses events and
    aggregation queries — consider denormalized counters for production)
  - Security-sensitive logging — destination URLs are not logged to
    avoid leaking query strings

Limitations of automation
- The AI creates a working implementation suitable for a prototype, but
  does not replace security, architecture or performance review.
- The test-suite should be run locally (Java 21 required); the assistant
  was unable to run the project's test-suite in its environment.

How to proceed when using this repo
1. Run the test-suite: `./mvnw clean verify` using Java 21.
2. Review failing tests (if any) and iterate with minimal, focused fixes.
3. Perform a security review before accepting into any shared CI/CD
   pipeline — pay attention to input validation and open redirect risks.
