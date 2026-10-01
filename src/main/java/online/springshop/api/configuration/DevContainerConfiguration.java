package online.springshop.api.configuration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@Profile("dev")
@Configuration(proxyBeanMethods = false)
public class DevContainerConfiguration
{
    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    PostgreSQLContainer<?> postgresContainer ()
    {
        return new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("springshop")
            .withUsername("springshop")
            .withPassword("springshop")
            .withReuse(true);
    }

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    RabbitMQContainer rabbitMQContainer ()
    {
        return new RabbitMQContainer("rabbitmq:4.1-management")
            .withAdminUser("springshop")
            .withAdminPassword("springshop");
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
