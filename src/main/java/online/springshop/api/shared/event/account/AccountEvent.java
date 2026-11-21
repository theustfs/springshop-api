package online.springshop.api.shared.event.account;

import lombok.Getter;
import lombok.Setter;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.util.Objects;

@Getter
@Setter
public sealed abstract class AccountEvent permits
    AccountRegisteredEvent, AccountPasswordUpdateEvent,
    AccountEmailUpdateStepOneEvent, AccountEmailUpdateStepTwoEvent,
    AccountDeletionEvent
{
    private final IdempotencyKey idempotencyKey;

    public AccountEvent (IdempotencyKey idempotencyKey)
    {
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Account event idempotency key must not be null");
    }
}
