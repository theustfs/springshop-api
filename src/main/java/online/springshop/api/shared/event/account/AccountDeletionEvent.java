package online.springshop.api.shared.event.account;

import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;
import java.time.Instant;

public record AccountDeletionEvent (
    IdempotencyKey idempotencyKey,
    String accountEmail,
    String confirmationCode,
    Duration confirmationCodeExpiration,
    Instant cancellationPeriod
) implements AccountEvent
{

}
