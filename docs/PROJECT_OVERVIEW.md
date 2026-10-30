# Springshop API — Project Overview

## Purpose

Springshop API is a portfolio e-commerce backend built with Spring Boot. The goal is to demonstrate a production-minded modular monolith: clear business boundaries, transactional consistency, secure APIs, asynchronous integration, automated tests, and a repeatable container deployment.

The repository is currently in its foundation stage. Runtime infrastructure, environment profiles, Testcontainers support, CI/CD workflows, deployment packaging, and an initial deny-by-default web security boundary exist. Business capabilities such as authentication, catalog, inventory, cart, ordering, and payment have not been implemented yet.

## Technology stack

| Area | Technology |
| --- | --- |
| Runtime | Java 17, Spring Boot 3.5 |
| HTTP API | Spring MVC, Bean Validation |
| Security | Spring Security, JJWT, Bouncy Castle |
| Persistence | Spring Data JPA, PostgreSQL 16, Flyway |
| Cache and transient state | Redis 8 |
| Messaging | RabbitMQ 4.1, Spring AMQP |
| Templates | Thymeleaf for HTML email templates |
| Mapping | MapStruct and Lombok |
| API documentation | springdoc OpenAPI and Swagger UI |
| Testing | JUnit, Spring Boot Test, Testcontainers |
| Packaging and deployment | Maven, Docker, Docker Compose, GitHub Actions |

## Current repository structure

```text
.
├── .github/workflows/       CI and release deployment workflows
├── docs/                    Architecture and project documentation
├── src/main/java/           Production application code
├── src/main/resources/      Shared, development, and production configuration
├── src/test/java/           Tests and development-time Testcontainers launcher
├── src/test/resources/      Test profile configuration
├── Dockerfile               Multi-stage production image
├── docker-compose.yaml      PostgreSQL, Redis, RabbitMQ, and API deployment
└── pom.xml                  Maven dependencies and build plugins
```

The production source currently contains the application entry point, shared configuration and the beginning of the authentication module's security configuration. Future business code should continue to be grouped by feature rather than by technical type:

```text
online.springshop.api
├── configuration
├── modules
│   ├── auth
│   │   ├── application
│   │   ├── domain
│   │   ├── infrastructure
│   │   │   └─ security
│   │   │      └─ configuration
│   │   │         └─ SpringSecurityConfiguration
│   │   └── interfaces
│   └── ...
└── shared
```

Each feature owns its web contracts, use cases, domain model, persistence adapters, and messaging adapters. Cross-feature dependencies should go through explicit application contracts or events. Shared code should remain small and contain concepts that genuinely apply to multiple modules.

## Current security boundary

`SpringSecurityConfiguration` defines the first HTTP security filter chain. It disables CSRF, uses stateless session management, and permits the Swagger UI, OpenAPI document, and All Actuator endpoints (`/docs/**`, `/swagger-ui/**`, `/v3/api-docs/**`, and `/actuator/**`). Every other request is denied by default. No authentication mechanism has been connected yet.

The API documentation remains disabled by default and is enabled by the development profile. Health, liveness, and readiness probes are publicly accessible so container and load-balancer checks can run without credentials; the production profile limits Actuator exposure to health endpoints only.

## Runtime dependencies

```text
Client / Load Balancer
          |
          v
   Springshop API
      |    |    \
      v    v     v
 PostgreSQL Redis RabbitMQ
```

- PostgreSQL is the source of truth for durable business state.
- Redis is intended for cache and short-lived state, not authoritative business data.
- RabbitMQ carries asynchronous integration events and background work.
- Flyway owns database schema evolution. Hibernate schema generation is disabled.

When asynchronous domain events are introduced, business state and an outbox record should be written in the same PostgreSQL transaction. Begin with an application-managed outbox publisher. Consumers must be idempotent because delivery is at least once. Introducing CDC tooling such as Debezium should require a documented operational need and an architecture decision.

## Application profiles

### Default

`application.yaml` contains safe shared defaults:

- Open Session in View is disabled.
- Hibernate does not create or update the schema.
- Flyway validates migrations.
- OpenAPI and Swagger UI are disabled unless explicitly enabled.

### Development

The `dev` profile enables verbose SQL logging, detailed Actuator information, and Swagger UI at `http://localhost:8080/docs`.

Development infrastructure is started through the test classpath so Testcontainers does not enter the production artifact:

```shell
./mvnw spring-boot:test-run "-Dspring-boot.run.profiles=dev"
```

This launches PostgreSQL, Redis, and RabbitMQ automatically. Docker must be running.

### Test

The `test` profile is used by the shared test base classes. Testcontainers supplies real PostgreSQL, Redis, and RabbitMQ instances where integration behavior is required.

### Production

The `prod` profile applies conservative Tomcat limits for a small EC2 instance, graceful shutdown, response compression, safe error responses, restricted Actuator exposure, and a readiness group covering PostgreSQL, Redis, and RabbitMQ.

The API container health check calls:

```text
/actuator/health/readiness
```

Production should run behind an ALB or trusted reverse proxy. Port 8080 should not be publicly accessible except through that proxy.

## Configuration and secrets

- Local and production values are supplied through environment variables.
- `.env.example` documents the variables expected by Docker Compose.
- `.env` is intentionally ignored and must never be committed.
- Production releases should use a specific image version or immutable digest. `latest` may be published as a convenience alias, but should not be the production deployment reference.
- New configuration groups should use validated `@ConfigurationProperties` classes rather than scattered `@Value` fields.

## Testing strategy

Use the smallest test scope that proves the behavior:

1. Plain unit tests for domain rules and application logic.
2. Spring-assisted unit tests only for configuration binding or framework integration.
3. JPA or Redis slice tests for persistence adapters.
4. Full integration tests for cross-layer workflows and infrastructure boundaries.

The repository provides reusable test bases under `../src/test/java/online/springshop/api/testsupport`. The scaffolding exists, but feature tests have not yet been added.

Run the complete verification suite with:

```shell
./mvnw clean verify
```

Integration tests require Docker.

## Build and deployment

The Dockerfile builds the application in one stage and runs it as an unprivileged user in a smaller JRE image. JVM heap percentages are container-aware, and the runtime image includes `curl` for the readiness probe.

Docker Compose runs the API and its three infrastructure services with persistent named volumes and dependency health checks. The release workflow builds and pushes AMD64 and ARM64 images when a GitHub release is published, then updates the EC2 deployment through SSH.

Target deployment behavior is:

1. Build and test once in CI.
2. Publish a release tag and record its digest.
3. Deploy that exact version.
4. Wait for the application readiness check.
5. Roll back to the previously known-good version if readiness fails.

## Current gaps and near-term roadmap

1. Add the first Flyway migration and application context smoke test.
2. Implement the first vertical business slice and establish module boundaries.
3. Connect authentication to the deny-by-default security policy and provide JSON security entry-point responses.
4. Implement authentication and authorization, and add security and token lifecycle tests.
5. Implement the transactional outbox when asynchronous events first become necessary.
6. Make release deployment pass the immutable release tag or digest to Compose and verify readiness.
7. Add dependency scanning, container scanning, SBOM generation, and structured production logging.
8. Document database backup and restore procedures.

Architecture described as future or target state in this document is not necessarily implemented. Source code and executable tests remain the authoritative record of current behavior.
