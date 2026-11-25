package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.recoverer;

import lombok.RequiredArgsConstructor;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitExchanges;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitQueues;
import online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants.RabbitRoutingKeys;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
public final class AccountOperationMessageRecoverer implements MessageRecoverer
{
    static final String RETRY_COUNT_HEADER = "x-account-operation-retry-count";

    private final AppProperties appProperties;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void recover (Message message, Throwable cause)
    {
        if (message == null)
            throw new NullPointerException("Message must not be null");

        if (isTerminal(cause))
        {
            sendToDeadLetterQueue(message);

            return;
        }

        int maximumRetries = appProperties.RABBITMQ().QUEUES().MESSAGING_ACCOUNT().retries();
        int retryCount = retryCount(message, maximumRetries);

        if (retryCount >= maximumRetries)
        {
            sendToDeadLetterQueue(message);

            return;
        }

        Message retryMessage = MessageBuilder.fromMessage(message)
            .setHeader(RETRY_COUNT_HEADER, retryCount + 1)
            .build();

        rabbitTemplate.send("", RabbitQueues.MESSAGING_ACCOUNT_RETRY_QUEUE, retryMessage);
    }

    private void sendToDeadLetterQueue (Message message)
    {
        rabbitTemplate.send(
            RabbitExchanges.ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE,
            RabbitRoutingKeys.ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY,
            message
        );
    }

    private int retryCount (Message message, int maximumRetries)
    {
        Object header = message.getMessageProperties().getHeaders().get(RETRY_COUNT_HEADER);

        if (header == null)
            return 0;

        if (header instanceof Number number)
            return Math.max(0, number.intValue());

        // A malformed retry header must not allow an unbounded retry loop.
        return maximumRetries;
    }

    private boolean isTerminal (Throwable cause)
    {
        for (Throwable current = cause; current != null; current = current.getCause())
        {
            if (current instanceof IllegalArgumentException
                || current instanceof NullPointerException
                || current instanceof MessageConversionException)
                return true;

            if (current instanceof HttpClientErrorException clientError
                && clientError.getStatusCode().value() != 408
                && clientError.getStatusCode().value() != 429)
                return true;
        }

        return false;
    }
}
