package online.springshop.api.shared.event.account;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;
import java.util.Objects;

@Getter
@Setter
@Accessors(fluent = true)
public final class AccountEmailUpdateStepTwoEvent extends AccountEvent
{
    private final String newEmail;
    private final String confirmationCode;
    private final Duration confirmationCodeExpiration;

    public AccountEmailUpdateStepTwoEvent (
        IdempotencyKey idempotencyKey,
        String newEmail,
        String confirmationCode,
        Duration confirmationCodeExpiration
    )
    {
        super(idempotencyKey);

        this.newEmail = Objects.requireNonNull(newEmail);
        this.confirmationCode = Objects.requireNonNull(confirmationCode);
        this.confirmationCodeExpiration = Objects.requireNonNull(confirmationCodeExpiration);
    }
}
