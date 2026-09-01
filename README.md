# Springshop API

## About the Project

Springshop API is a RESTful e-commerce backend developed as a portfolio project.
It demonstrates how to build a production-inspired application using the Spring ecosystem
while following clean architecture principles and backend best practices.

## Built with

[![Springboot][springboot-shield]]()
[![Spring security][spring-security-shield]]()
[![PostgreSQL][postgresql-shield]]()
[![Docker][docker-shield]]()
[![Rabbitmq][rabbitmq-shield]]()
[![Redis][redis-shield]]()

## Installation

### Requirements

- Java 17
- Docker

### How to install and run

1. Clone the repository
    ```shell
    git clone https://github.com/theustfs/springshop-api
    ```

2. Enter the project directory
    ```shell
    cd springshop-api
    ```

3. Run the application
    ```shell
    ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
    ```

4. Open the [API documentation][api-documentation]

<!-- MARKDOWN LINKS & IMAGES -->
[springboot-shield]: https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white
[spring-security-shield]: https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white
[postgresql-shield]: https://img.shields.io/badge/PostgreSQL-blue?style=for-the-badge
[docker-shield]: https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white
[rabbitmq-shield]: https://img.shields.io/badge/rabbitmq-%23FF6600.svg?&style=for-the-badge&logo=rabbitmq&logoColor=white
[redis-shield]: https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white
[api-documentation]: http://localhost:8080/docs
