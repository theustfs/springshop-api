package online.springshop.api.shared.event.account;

import lombok.Getter;
import lombok.Setter;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Duration;
import java.util.Objects;

@Getter
@Setter
public final class AccountPasswordUpdateEvent extends AccountEvent
{
    private final String accountEmail;
    private final String confirmationCode;
    private final Duration confirmationCodeExpiration;

    public AccountPasswordUpdateEvent (
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
