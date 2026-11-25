package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants;

public final class RabbitQueues
{
    public static final String MESSAGING_ACCOUNT_QUEUE = "messaging.account";
    public static final String MESSAGING_ACCOUNT_RETRY_QUEUE = "messaging.account.retry.queue";
    public static final String MESSAGING_ACCOUNT_DEAD_LETTER_QUEUE = "messaging.account.dlq";

    private RabbitQueues () {}
}
