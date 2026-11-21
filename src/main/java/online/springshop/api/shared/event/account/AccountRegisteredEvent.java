package online.springshop.api.shared.event.account;

import lombok.Getter;
import lombok.Setter;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;
import java.util.Objects;

@Setter
@Getter
public final class AccountRegisteredEvent extends AccountEvent
{
    private final String accountEmail;
    private final String confirmationCode;
    private final Duration confirmationCodeExpiration;

    public AccountRegisteredEvent (
        IdempotencyKey idempotencyKey,
        String accountEmail,
        String confirmationCode,
        Duration confirmationCodeExpiration
    )
    {
        super(idempotencyKey);

        this.accountEmail = Objects.requireNonNull(accountEmail);
        this.confirmationCode = Objects.requireNonNull(confirmationCode);
        this.confirmationCodeExpiration = Objects.requireNonNull(confirmationCodeExpiration);
    }
}
