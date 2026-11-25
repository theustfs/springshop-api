package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.recoverer;

import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.application.port.MessagingEventRepository;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitExchanges;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitQueues;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitRoutingKeys;
import online.springshop.api.modules.messaging.interfaces.event.rabbitmq.dispatcher.AccountOperationEventDispatcher;
import online.springshop.api.shared.event.account.AccountRegisteredEvent;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import online.springshop.api.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Account operation message recoverer integration tests")
class AccountOperationMessageRecovererIntegrationTest extends AbstractIntegrationTest
{
    @MockitoBean
    private MessagingEventRepository messagingEventRepository;

    @MockitoBean
    private AccountOperationEventDispatcher dispatcher;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private void messagingEventRepositoryStub ()
    {
        doReturn(Optional.empty())
            .when(messagingEventRepository)
            .findByIdAndLock(any());

        doNothing()
            .when(messagingEventRepository).save(any());

        doReturn(true)
            .when(messagingEventRepository)
            .saveAndClaim(any());
    }

    @Test
    @DisplayName("Should route non-terminal listener failures to the retry queue")
    void shouldSendNonTerminalFailuresToRetryQueue ()
    {
        messagingEventRepositoryStub();

        doThrow(new RuntimeException("temporary provider failure"))
            .when(dispatcher).dispatch(any());

        sendAccountRegisteredEvent();

        Message retryMessage = rabbitTemplate.receive(
            RabbitQueues.MESSAGING_ACCOUNT_RETRY_QUEUE,
            5_000
        );

        assertNotNull(retryMessage, "Expected the failed message in the retry queue");
        assertEquals(
            1,
            retryMessage.getMessageProperties().getHeaders()
                .get(AccountOperationMessageRecoverer.RETRY_COUNT_HEADER)
        );
    }

    @Test
    @DisplayName("Should route terminal listener failures to the dead-letter queue")
    void shouldSendTerminalFailuresToDeadLetterQueue ()
    {
        messagingEventRepositoryStub();

        doThrow(new IllegalArgumentException("unsupported event"))
            .when(dispatcher).dispatch(any());

        sendAccountRegisteredEvent();

        Message deadLetterMessage = rabbitTemplate.receive(
            RabbitQueues.MESSAGING_ACCOUNT_DEAD_LETTER_QUEUE,
            5_000
        );

        assertNotNull(deadLetterMessage, "Expected the failed message in the dead-letter queue");
        assertEquals(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            deadLetterMessage.getMessageProperties().getReceivedExchange()
        );
        assertEquals(
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            deadLetterMessage.getMessageProperties().getReceivedRoutingKey()
        );
    }

    @Test
    @DisplayName("Should return a retry message to the main queue after the configured delay")
    void shouldReturnRetryMessageToMainQueueAfterConfiguredDelay ()
    {
        messagingEventRepositoryStub();

        doThrow(new RuntimeException("temporary provider failure"))
            .doNothing()
            .when(dispatcher).dispatch(any());

        sendAccountRegisteredEvent();

        verify(
            dispatcher,
            timeout(appProperties.RABBITMQ().QUEUES().MESSAGING_ACCOUNT().retryDelay().toMillis() + 500)
                .times(2)
        ).dispatch(any());
    }

    private void sendAccountRegisteredEvent ()
    {
        rabbitTemplate.convertAndSend(
            RabbitExchanges.ACCOUNT_OPERATION_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_ROUTING_KEY,
            new AccountRegisteredEvent(
                new IdempotencyKey(UUID.randomUUID()),
                "test@example.com",
                "123456",
                Duration.ofMinutes(15)
            )
        );
    }
}
