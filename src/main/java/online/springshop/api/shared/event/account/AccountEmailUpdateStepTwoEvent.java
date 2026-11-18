package online.springshop.api.shared.event.account;

import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;

public record AccountEmailUpdateStepTwoEvent (
    IdempotencyKey idempotencyKey,
    String newEmail,
    String confirmationCode,
    Duration confirmationCodeExpiration
) implements AccountEvent
{

}
