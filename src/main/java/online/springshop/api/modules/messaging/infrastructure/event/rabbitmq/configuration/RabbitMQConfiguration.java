package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.configuration;

import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.recoverer.AccountOperationMessageRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfiguration
{
    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter ()
    {
        DefaultClassMapper classMapper = new DefaultClassMapper();

        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter();

        classMapper.setTrustedPackages(
            "online.springshop.api.shared.event.account"
        );

        messageConverter.setClassMapper(classMapper);

        return messageConverter;
    }

    @Bean
    RabbitTemplate RabbitTemplate (
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter jackson2JsonMessageConverter
    )
    {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);

        return rabbitTemplate;
    }

    @Bean
    RetryOperationsInterceptor accountOperationRetryInterceptor (
        AccountOperationMessageRecoverer accountOperationMessageRecoverer
    )
    {
        return RetryInterceptorBuilder.stateless()
            .maxAttempts(1)
            .recoverer(accountOperationMessageRecoverer)
            .build();
    }

    @Bean
    SimpleRabbitListenerContainerFactory accountOperationListenerContainerFactory (
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter jackson2JsonMessageConverter,
        RetryOperationsInterceptor accountOperationRetryInterceptor
    )
    {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setAdviceChain(accountOperationRetryInterceptor);

        return factory;
    }
}
