# ---------- Build ---------- #
FROM maven:3.9.11-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean verify

# ---------- Runtime ---------- #
FROM eclipse-temurin:17-jre-jammy

RUN groupadd -r springshop && \
    useradd -r -g springshop springshop

WORKDIR /app

EXPOSE 8080

COPY --from=builder --chown=springshop:springshop \
    /build/target/*.jar app.jar

USER springshop

ENTRYPOINT [\
    "java",\
    "-XX:MaxRAMPercentage=70",\
    "-XX:InitialRAMPercentage=20",\
    "-XX:+ExitOnOutOfMemoryError",\
    "-jar",\
    "app.jar"\
]