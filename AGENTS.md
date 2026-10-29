# AGENTS.md

This file applies to the entire repository. It defines how coding agents should inspect, change, and verify Springshop API.

## Start here

Before changing code:

1. Read `README.md` and `docs/PROJECT_OVERVIEW.md`.
2. Inspect `git status` and preserve unrelated user changes.
3. Inspect the affected module, configuration profile, migration, and tests.
4. Treat source code and tests as authoritative. Do not present planned architecture as implemented behavior.

The project is currently in its foundation stage. Do not assume that domain modules, database tables, security flows, queues, or API contracts exist until they are present in the repository.

## Build and run commands

Use the Maven Wrapper rather than a system Maven installation.

```shell
# Full verification; Docker is required for Testcontainers-based tests
./mvnw clean verify

# Development application with PostgreSQL, Redis, and RabbitMQ
./mvnw spring-boot:test-run "-Dspring-boot.run.profiles=dev"

# Build the production image
docker build -t springshop-api:local .

# Validate the resolved Compose configuration
docker compose config
```

Do not move Testcontainers dependencies to compile scope. The development launcher intentionally lives in `src/test` and uses the test runtime classpath so the production JAR stays free of Testcontainers.

## Architecture and package boundaries

- Organize production code by business feature under `online.springshop.api.modules`.
- Inside a feature, use `application`, `domain`, `infrastructure` and `interfaces` packages when those boundaries provide real value.
- Keep domain objects independent of Spring, HTTP, persistence, and broker APIs where practical.
- Controllers translate HTTP contracts and delegate to application use cases. They must not contain business workflows.
- Do not expose JPA entities directly from controllers.
- Repositories, broker clients, and external clients are infrastructure details.
- Avoid cross-module access to another module's repositories or internal domain objects. Use an explicit application contract or an event.
- Add shared abstractions only after at least two concrete consumers demonstrate the common need.
- Prefer a modular monolith. Do not introduce a new deployable service without an explicit requirement and architecture decision.

## Java conventions

- Target Java 17 and APIs supported by the configured Spring Boot version.
- Prefer constructor injection. Do not use field injection.
- Keep classes and methods package-private unless they form an intentional public boundary.
- Use the injected `Clock` for business time; do not call `Instant.now()` or system time directly in domain/application logic.
- Use Bean Validation at input boundaries and enforce business invariants in the domain model.
- Prefer immutable request/response DTOs and value objects.
- Use MapStruct for non-trivial boundary mappings; do not hide business decisions inside mappers.
- Lombok is acceptable for mechanical code, but avoid annotations that weaken entity identity or expose mutable state unintentionally.
- Never log passwords, tokens, authorization headers, reset codes, or personal data.

Follow the formatting already established in the edited file. Avoid broad formatting-only rewrites mixed with behavioral changes.

## Persistence and migrations

- PostgreSQL is the authoritative durable store.
- Every schema change requires a Flyway migration under `src/main/resources/db/migration`.
- Use immutable, forward-only migration filenames such as `V1__create_user_schema.sql`.
- Never edit an applied migration. Add a new migration instead.
- Keep `spring.jpa.hibernate.ddl-auto=none`; tests and production must exercise Flyway-managed schemas.
- Place transaction boundaries in application services, not controllers.
- Avoid long transactions and external network calls inside database transactions.
- Consider indexes, constraints, locking behavior, pagination, and query count with every persistence change.

## Messaging and the outbox

- RabbitMQ is the application message broker.
- When a database change and event publication must be atomic, write an outbox record in the same PostgreSQL transaction.
- Start with an application-managed outbox publisher. Do not add Debezium, Kafka, or another infrastructure component without a documented need.
- Treat delivery as at least once. Events need stable IDs and consumers need idempotency.
- Version event contracts deliberately. Do not reuse persistence entities as event payloads.
- Configure bounded retries and dead-letter handling; never create infinite immediate retry loops.

## Security

- Spring Security must protect API endpoints by default. Make public endpoints explicit.
- Authorization belongs at the HTTP boundary and, where required, the application/service boundary.
- Validate JWT issuer, audience, signature algorithm, timestamps, and key material.
- Do not put secrets or realistic credentials in source, tests, examples, logs, images, or Compose files.
- Thymeleaf is intended for HTML email rendering. Do not add `thymeleaf-extras-springsecurity6` unless templates actually use Spring Security expressions.
- Keep production error responses free of stack traces, exception types, and internal messages.
- Preserve restricted production Actuator exposure and health-detail settings.

## Configuration

- Shared safe defaults belong in `application.yaml`.
- Developer-only diagnostics belong in `application-dev.yaml`.
- Tests use `application-test.yaml`.
- Production hardening belongs in `application-prod.yaml`.
- Prefer validated `@ConfigurationProperties` records/classes for new configuration groups.
- Environment-specific values and secrets must come from environment variables or a secret manager.
- Update `.env.example` when required Compose variables change, but use placeholders rather than working secrets.
- If changing forwarded-header handling, assume the application is behind a trusted ALB or reverse proxy and document any network-security implications.

## Testing requirements

- Add or update tests for every behavioral change.
- Prefer plain unit tests for domain behavior; do not start Spring or containers unnecessarily.
- Use the existing JPA, Redis, Spring, or integration test bases only when the corresponding scope is required.
- Keep tests deterministic. Inject `Clock`, control generated identifiers where assertions require it, and avoid arbitrary sleeps.
- Test success, validation failure, authorization failure, duplicate/idempotent handling, and relevant concurrency behavior.
- A passing test suite with zero relevant assertions is not sufficient.
- Run `./mvnw clean verify` before handing off whenever Java and Docker are available. If verification cannot run, state exactly what was not verified and why.

## Docker and deployment

- Keep the runtime container unprivileged.
- Do not copy `.env`, repository metadata, IDE files, or build outputs into the image context.
- Production Compose must deploy a specific release version or digest, not rely on `latest`.
- Preserve the API readiness check at `/actuator/health/readiness` when changing Actuator or security rules.
- Ensure Docker's stop grace period is longer than Spring's shutdown timeout when adding deployment settings.
- Database, Redis, and RabbitMQ volumes are persistent data. Never remove or recreate them as part of routine automation.
- Deployment changes must retain a clear rollback path to the previous known-good image.

## Change discipline

- Keep changes focused on the requested outcome.
- Preserve user-authored work and avoid destructive Git commands.
- Do not silently change public API contracts, database semantics, event schemas, deployment requirements, or security policy.
- Update documentation when commands, profiles, configuration variables, architecture boundaries, or operational behavior change.
- Record a short architecture decision in `docs/adr/` before introducing a new datastore, broker, CDC platform, deployable service, or cross-cutting framework.

## Definition of done

A change is complete when:

1. The requested behavior is implemented.
2. Relevant automated tests exist and pass, or verification limitations are reported.
3. Configuration is safe across development, test, and production profiles.
4. Database and event compatibility have been considered.
5. Security and secret-handling implications have been considered.
6. Operational health checks and rollback behavior remain valid.
7. Documentation reflects any user-visible or operational change.
