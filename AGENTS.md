# Repository Overview

## Project Description

**Springshop API** is a RESTful e-commerce backend (portfolio project) built to demonstrate production-inspired development with the Spring ecosystem, following clean architecture principles and backend best practices.

- **Language/Runtime:** Java 17
- **Framework:** Spring Boot 3.5.x (Web, Security, Data JPA, Data Redis, AMQP, Actuator, Thymeleaf, Validation)
- **Database:** PostgreSQL 16 with Flyway migrations (`ddl-auto: none` — schema is managed exclusively by Flyway)
- **Infrastructure:** RabbitMQ 4.1 (async messaging), Redis 8 (caching)
- **Auth:** Spring Security with JWT (jjwt 0.13) + BouncyCastle
- **Mapping:** MapStruct 1.6 with Lombok (via `lombok-mapstruct-binding`)
- **API docs:** springdoc-openapi (Swagger UI at `/docs`, enabled only in `dev` profile)
- **Testing:** JUnit 5, Testcontainers (Postgres/Rabbit/Redis), JavaFaker

## Architecture Overview

- **Profiles drive infrastructure:**
  - `dev` — Testcontainers beans (`DevContainerConfiguration`) auto-start Postgres, RabbitMQ, Redis via `@ServiceConnection`; no external setup needed.
  - `prod` — expects externally provided Postgres/Redis/RabbitMQ (see `docker-compose.yaml`); Tomcat/Hikari tuning applied.
  - `test` — Testcontainers for integration tests (`TestContainerConfiguration`).
  - (base `application.yaml`) — API docs and swagger disabled by default; enabled only in `dev`.
- **Data flow (planned):** REST controllers → service layer → JPA repositories (PostgreSQL); async work via RabbitMQ; caching via Redis. Note: the project currently contains only the application skeleton, configuration, and test infrastructure — no domain features are implemented yet.
- **Clock:** A `Clock` bean (`appClock`, system UTC) is exposed for injection — use it instead of `LocalDate.now()`/`Instant.now()` in domain code.
- **Conventions baked into config:**
  - `spring.jpa.open-in-view: false` (no lazy loading in controllers)
  - `spring.jackson.default-property-inclusion: non_null` (DTOs omit nulls)
  - Flyway `baseline-on-migrate` and `validate-on-migrate` enabled
- **Deployment:** Multi-stage `Dockerfile` (Maven build → JRE 17 runtime, non-root user). Docker Hub image released via GitHub Actions on published releases. `docker-compose.yaml` wires DB + Redis + Rabbit + app for production.

## Directory Structure

```
springshop-api/
├── pom.xml                              # Maven build (Java 17, Spring Boot 3.5)
├── Dockerfile                           # Multi-stage prod image
├── docker-compose.yaml                  # prod stack: postgres, redis, rabbit, app
├── .env.example                         # env vars for docker-compose (copy to .env)
├── .github/workflows/
│   ├── ci.yaml                          # runs `./mvnw clean verify`
│   └── deploy.yaml                      # builds/pushes image on release
├── .mvn/                                # Maven wrapper (3.9.16)
└── src/
    ├── main/java/online/springshop/api/
    │   ├── SpringshopApplication.java   # entry point
    │   └── configuration/
    │       ├── AppConfiguration.java            # Clock bean
    │       └── DevContainerConfiguration.java   # @Profile("dev") containers
    ├── main/resources/
    │   ├── application.yaml               # base config
    │   ├── application-dev.yaml           # docs enabled, verbose SQL logging
    │   └── application-prod.yaml          # server/compression tuning
    └── test/
        ├── java/online/springshop/api/configuration/
        │   ├── AbstractIntegrationTest.java   # @SpringBootTest base class
        │   ├── AbstractDataJpaTest.java       # @DataJpaTest base class
        │   ├── AbstractDataRedisTest.java     # @DataRedisTest base class
        │   ├── AbstractSpringUnitTest.java    # lightweight unit base class
        │   └── TestContainerConfiguration.java# @Profile("test") containers
        └── resources/application-test.yaml    # test profile (Flyway clean allowed)
```

## Development Workflow

**Prerequisites:** Java 17, Docker (only needed for `dev` profile and tests).

### Run locally

```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
# Swagger UI: http://localhost:8080/docs
# Actuator:   http://localhost:8080/actuator
```

The `dev` profile self-manages Postgres, RabbitMQ, and Redis via Testcontainers — no local services required.

### Build / test

```bash
./mvnw clean verify   # full build + all tests (same as CI)
./mvnw test           # tests only
./mvnw test -Dtest=SomeTest   # single test class
```

CI (`.github/workflows/ci.yaml`) runs `./mvnw clean verify` on pushes/PRs to `main` and `development`.

### Testing approach

- Extend the existing abstract base classes in `src/test/.../configuration/` (`AbstractIntegrationTest`, `AbstractDataJpaTest`, `AbstractDataRedisTest`, `AbstractSpringUnitTest`) so tests automatically get the `test` profile and containerized infrastructure.
- Integration tests use Testcontainers (no pre-installed DB/broker needed); JavaFaker is available for data generation.
- Tests require Docker to be running locally.

### Docker (production-style stack)

```bash
cp .env.example .env   # fill in DB/Redis/Rabbit credentials and image tags
docker compose up
```

### Lint / format

- No checkstyle/spotless plugin is configured; follow existing code style: 4-space indentation, opening braces on new lines, no spaces before method parentheses (e.g., `method ()`), no semicolon omission — semicolons are used normally. JavaFaker/MapStruct/Lombok annotation processors are already wired in the compiler plugin config.

### Things to keep in mind when making changes

- Schema changes must be Flyway migrations (Hibernate DDL is disabled).
- Keep `ddl-auto: none` and `open-in-view: false` behavior in mind: fetch data explicitly in services/repositories.
- Use MapStruct mappers for entity ↔ DTO conversion; annotate new classes with Lombok where appropriate (binding processor is configured).
- New infra-sensitive code (JPA, Redis, Rabbit) should be testable via the existing Testcontainers base classes.
- Do not commit `.env` (gitignored); `.env.example` documents required variables.
