package online.springshop.api.configuration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@Profile("test")
@Configuration(proxyBeanMethods = false)
public class TestContainerConfiguration
{
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer ()
    {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitMQContainer ()
    {
        return new RabbitMQContainer("rabbitmq:4.1");
    }

    @Bean
    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer ()
    {
        return new GenericContainer<>("redis:8-alpine")
            .withExposedPorts(6379);
    }
}
