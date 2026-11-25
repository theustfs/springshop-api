package online.springshop.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties (
    RESEND RESEND,
    MAIL MAIL,
    RABBITMQ RABBITMQ
)
{
    public record RESEND (
        String API_KEY,
        String BASE_URL
    )
    {

    }

    public record MAIL (
        String FROM
    )
    {

    }

    public record RABBITMQ (
        QUEUES QUEUES
    )
    {
        public record QUEUES (
            QUEUE MESSAGING_ACCOUNT
        )
        {

        }

        public record QUEUE (
            Duration retryDelay,
            int retries
        )
        {

        }
    }
}
