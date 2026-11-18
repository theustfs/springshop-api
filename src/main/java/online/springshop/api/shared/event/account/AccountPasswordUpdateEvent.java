package online.springshop.api.shared.event.account;

import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;

public record AccountPasswordUpdateEvent (
    IdempotencyKey idempotencyKey,
    String accountEmail,
    String confirmationCode,
    Duration confirmationCodeExpiration
) implements AccountEvent
{

}
