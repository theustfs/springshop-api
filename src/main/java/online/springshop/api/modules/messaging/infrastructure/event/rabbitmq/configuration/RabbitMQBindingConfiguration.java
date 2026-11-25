package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.configuration;

import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitExchanges;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitQueues;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitRoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQBindingConfiguration
{
    // QUEUES

    @Bean
    Queue messagingAccountQueue ()
    {
        return QueueBuilder.durable(RabbitQueues.MESSAGING_ACCOUNT_QUEUE)
            .build();
    }

    @Bean
    Queue messagingAccountRetryQueue (
        AppProperties appProperties
    )
    {
        int timeToLive = (int) (appProperties.RABBITMQ().QUEUES().MESSAGING_ACCOUNT().retryDelay().toMillis());

        return QueueBuilder.durable(RabbitQueues.MESSAGING_ACCOUNT_RETRY_QUEUE)
            .deadLetterExchange(RabbitExchanges.ACCOUNT_OPERATION_EXCHANGE)
            .deadLetterRoutingKey(RabbitRoutingKeys.ACCOUNT_OPERATION_ROUTING_KEY)
            .ttl(timeToLive)
            .build();
    }

    @Bean
    Queue messagingAccountDeadLetterQueue ()
    {
        return QueueBuilder.durable(RabbitQueues.MESSAGING_ACCOUNT_DEAD_LETTER_QUEUE)
            .build();
    }

    // EXCHANGES

    @Bean
    TopicExchange accountOperationExchange ()
    {
        return new TopicExchange(
            RabbitExchanges.ACCOUNT_OPERATION_EXCHANGE,
            true,
            false
        );
    }

    @Bean
    TopicExchange accountOperationDeadLetterExchange ()
    {
        return new TopicExchange(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            true,
            false
        );
    }

    // BINDINGS

    @Bean
    Binding messagingAccountBinding (
        TopicExchange accountOperationExchange,
        Queue messagingAccountQueue
    )
    {
        return BindingBuilder.bind(messagingAccountQueue)
            .to(accountOperationExchange)
            .with(RabbitRoutingKeys.ACCOUNT_OPERATION_ROUTING_KEY);
    }

    @Bean
    Binding messagingAccountDeadLetterBinding (
        TopicExchange accountOperationDeadLetterExchange,
        Queue messagingAccountDeadLetterQueue
    )
    {
        return BindingBuilder.bind(messagingAccountDeadLetterQueue)
            .to(accountOperationDeadLetterExchange)
            .with(RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY);
    }
}
