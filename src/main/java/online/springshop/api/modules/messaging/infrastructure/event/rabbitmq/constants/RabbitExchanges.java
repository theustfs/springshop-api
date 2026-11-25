package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants;

public final class RabbitExchanges
{
    public static final String ACCOUNT_OPERATION_EXCHANGE = "account.operation.ex";
    public static final String ACCOUNT_OPERATION_DEAD_LETTER_EXCHANGE = "account.operation.dlx";

    private RabbitExchanges () {}
}
