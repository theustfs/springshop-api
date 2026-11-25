package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.recoverer;

import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitExchanges;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitQueues;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitRoutingKeys;
import online.springshop.api.testsupport.AbstractSpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@Import({
    AccountOperationMessageRecoverer.class
})
@DisplayName("Account operation message recoverer unit tests")
class AccountOperationMessageRecovererUnitTest extends AbstractSpringUnitTest
{
    private static final String RETRY_HEADER = "x-account-operation-retry-count";

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private AccountOperationMessageRecoverer recoverer;

    private Message messageWithRetryCount (int retryCount)
    {
        Message message = MessageBuilder.withBody(new byte[]{1}).build();
        message.getMessageProperties().setHeader(RETRY_HEADER, retryCount);

        return message;
    }

    @Test
    @DisplayName("Should route non-terminal failures to the retry queue and increment the retry count")
    void retriesNonTerminalFailureAndIncrementsRetryCount ()
    {
        Message message = messageWithRetryCount(1);

        recoverer.recover(message, new RuntimeException("provider unavailable"));

        verify(rabbitTemplate).send(
            "",
            RabbitQueues.MESSAGING_ACCOUNT_RETRY_QUEUE,
            MessageBuilder.fromMessage(message).setHeader(RETRY_HEADER, 2).build()
        );
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should route terminal failures directly to the dead-letter exchange")
    void routesTerminalFailureDirectlyToDeadLetterExchange ()
    {
        recoverer.recover(messageWithRetryCount(0), new IllegalArgumentException("invalid event"));

        verify(rabbitTemplate).send(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            messageWithRetryCount(0)
        );

        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should dead-letter non-terminal failures after the configured retry count is reached")
    void deadLettersNonTerminalFailureOnceConfiguredRetryCountIsReached ()
    {
        recoverer.recover(messageWithRetryCount(3), new RuntimeException("provider unavailable"));

        verify(rabbitTemplate).send(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            messageWithRetryCount(3)
        );
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should publish exactly the configured number of retries before dead-lettering")
    void respectsConfiguredMaximumRetryCount ()
    {
        Message message = messageWithRetryCount(0);

        for (int retryCount = 0; retryCount < 3; retryCount++)
        {
            recoverer.recover(message, new RuntimeException("provider unavailable"));
            message = MessageBuilder.fromMessage(message)
                .setHeader(RETRY_HEADER, retryCount + 1)
                .build();
        }

        recoverer.recover(message, new RuntimeException("provider unavailable"));

        for (int i = 1; i <= appProperties.RABBITMQ().QUEUES().MESSAGING_ACCOUNT().retries(); i++)
            verify(rabbitTemplate, times(1)).send(
                "",
                RabbitQueues.MESSAGING_ACCOUNT_RETRY_QUEUE,
                MessageBuilder.fromMessage(messageWithRetryCount(0)).setHeader(RETRY_HEADER, i).build()
            );

        verify(rabbitTemplate, times(1)).send(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            message
        );
        verifyNoMoreInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Should dead-letter a message with a malformed retry count")
    void malformedRetryCountCannotCauseAnUnboundedRetry ()
    {
        Message message = MessageBuilder.withBody(new byte[]{1})
            .setHeader(RETRY_HEADER, "not-a-number")
            .build();

        recoverer.recover(message, new RuntimeException("provider unavailable"));

        verify(rabbitTemplate).send(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            message
        );
    }
}
