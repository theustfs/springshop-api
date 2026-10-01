---

## invokable: true

Review this code for potential issues.

Focus on correctness, security, transactional behavior, persistence, messaging, caching, testability, and consistency with the existing architecture.

Do not suggest changes merely for stylistic preference when the existing implementation is already correct and consistent with the project.

## Spring / JPA correctness

* Respect `spring.jpa.open-in-view: false`.

    * Controllers and serialization must not rely on lazy-loading outside a transaction.
    * Fetch required relationships explicitly in repositories/services.
    * Flag potential `LazyInitializationException` scenarios.
* Review transaction boundaries:

    * verify that operations requiring atomicity have an appropriate `@Transactional` boundary;
    * check transaction propagation and rollback behavior when relevant;
    * flag self-invocation when it prevents Spring's transactional proxy from being applied;
    * do not require `@Transactional` on methods that do not need a transaction.
* Hibernate must not manage the application schema.

    * `ddl-auto` must remain `none`;
    * schema changes must be implemented through Flyway migrations.
* Look for N+1 query patterns.

    * Consider `@EntityGraph`, JPQL fetch joins, projections, or other appropriate query strategies.
    * Do not recommend eager-loading relationships globally as a generic solution.
* Check entity mappings for incorrect cascade, orphan-removal, fetch, nullable, uniqueness, and relationship configuration.
* Check repository queries for correctness and unnecessary database access.

## Spring Security / Authentication

* Verify that endpoints have the intended authentication and authorization requirements.
* Check that security rules do not accidentally expose protected endpoints or block intended public endpoints.
* For JWT handling, check:

    * signature validation;
    * expiration validation;
    * issuer validation when configured/required;
    * appropriate claims validation;
    * secure secret/key configuration;
    * token leakage through logs, exceptions, URLs, or responses.
* Secrets and cryptographic keys must not be hard-coded.
* Sensitive information such as passwords, authentication tokens, and unnecessary PII must not be logged or returned by API responses.
* Passwords must never be stored or returned in plaintext.

## REST APIs / DTOs / Validation

* Request DTOs should use Jakarta Bean Validation where validation is required.
* Check appropriate use of `@Valid` / `@Validated`.
* Backend validation is authoritative; do not rely solely on frontend validation.
* Check response DTOs for accidental exposure of:

    * passwords;
    * authentication credentials;
    * internal security data;
    * unnecessary PII;
    * persistence implementation details.
* Prefer the project's MapStruct mapping approach for non-trivial entity ↔ DTO mappings.
* Do not require MapStruct for trivial transformations where it would add unnecessary complexity.
* Check HTTP status codes and error responses for consistency with the existing API.
* Avoid leaking internal exception messages, stack traces, or infrastructure details to clients.

## Time / Clock

* Domain/application logic involving the current time should use the application's injected `Clock` bean.
* Flag direct use of:

    * `LocalDate.now()`;
    * `LocalDateTime.now()`;
    * `Instant.now()`;
    * `ZonedDateTime.now()`;
    * `OffsetDateTime.now()`;
      when they bypass the application's clock abstraction.
* Time-dependent tests should use the injected/fixed `Clock` rather than depending on the real system clock.
* Do not flag APIs that explicitly receive a time value as an argument or code where using the system clock is intentionally required at an infrastructure boundary.

## RabbitMQ / Application Events

* Distinguish application/domain events from RabbitMQ message listeners.
* Review `@RabbitListener` consumers for:

    * acknowledgement behavior;
    * retry/reprocessing behavior;
    * exception handling;
    * rejection behavior;
    * idempotency where duplicate delivery is possible;
    * appropriate dead-letter/recovery configuration according to the project's messaging strategy.
* Do not assume every listener requires a DLQ if the existing messaging configuration intentionally handles failures another way.
* Check message payloads for compatibility with the application's event contracts.
* Avoid publishing sensitive information unnecessarily.
* Check transactional boundaries when database changes and message publication must remain consistent.
* For `@EventListener` / application events, verify that failures and transaction timing are appropriate for the intended semantics.

## Redis / Caching

* Check Redis key construction for consistency and collision risks.
* Check serialization/deserialization compatibility.
* Review TTL configuration where cached or temporary data is expected to expire.
* Check cache invalidation when the underlying data changes.
* Do not require TTL on Redis data that is intentionally persistent.
* Avoid caching sensitive data unless the security and invalidation behavior are understood.
* Check whether cache reads can return stale data in ways that violate application requirements.

## Testing

* New behavior should have appropriate tests.
* Use the existing test base classes when the test requires their configured Spring/Testcontainers environment:

    * `AbstractIntegrationTest`
    * `AbstractDataJpaTest`
    * `AbstractDataRedisTest`
    * `AbstractSpringUnitTest`
* Pure unit tests do not need to extend an integration/test-container base class when they do not require Spring or infrastructure.
* Infrastructure-dependent tests must not assume locally installed PostgreSQL, RabbitMQ, or Redis instances.
* Use the existing Testcontainers configuration.
* Avoid hard-coded credentials or environment-specific assumptions.
* Check assertions for meaningful behavior rather than implementation details.
* Check error paths, transactional behavior, persistence behavior, and message-processing failures where relevant.
* Time-dependent tests should use the application's controllable `Clock`.
* Flag tests that are unnecessarily slow or flaky.

## Build / Configuration / Operations

* Review `pom.xml` for:

    * duplicated dependencies;
    * conflicting versions;
    * incorrect scopes;
    * unnecessary dependencies;
    * dependencies that should be optional or test-scoped.
* Respect Spring Boot dependency management rather than overriding versions unnecessarily.
* Configuration should remain environment-driven.
* Do not commit secrets or real environment credentials.
* Do not break the assumptions of:

    * `application.yaml`;
    * `application-dev.yaml`;
    * `application-test.yaml`;
    * `application-prod.yaml`;
    * `docker-compose.yaml`;
    * `Dockerfile`.
* Production containers should continue to run as a non-root user.
* Avoid introducing infrastructure dependencies that are unavailable in the project's configured profiles.

## Architecture & Maintainability

* Follow the existing package/module boundaries and architectural conventions.
* Keep domain logic out of controllers and infrastructure-specific configuration where appropriate.
* Avoid coupling domain logic unnecessarily to Spring, JPA, Redis, or RabbitMQ.
* Avoid introducing abstractions solely for theoretical future requirements.
* Do not perform unrelated architectural refactors as part of a feature or bug fix.

## Review Output

For each meaningful issue:

1. Identify the file/class/method when possible.
2. Explain why it is a problem.
3. Explain the concrete consequence or failure mode.
4. Provide a specific recommendation.
5. Distinguish bugs/security issues from maintainability suggestions.
6. Do not recommend unrelated refactors.

Prioritize findings:

* **Critical** — severe security issue, data loss, or fundamentally broken behavior.
* **High** — likely runtime failure, serious data-integrity issue, or significant security problem.
* **Medium** — correctness, reliability, or maintainability issue that should be addressed.
* **Low** — minor consistency, accessibility, or style improvement.

If no meaningful issues are found, explicitly state that no significant issues were identified rather than inventing findings.
