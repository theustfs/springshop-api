package online.springshop.api.modules.messaging.infrastructure.event.rabbitmq.constants;

public final class RabbitRoutingKeys
{
    public static final String ACCOUNT_OPERATION_ROUTING_KEY = "account.operation.rk";
    public static final String ACCOUNT_OPERATION_DEAD_LETTER_ROUTING_KEY = "account.operation.retry.dlrk";

    private RabbitRoutingKeys () {}
}
