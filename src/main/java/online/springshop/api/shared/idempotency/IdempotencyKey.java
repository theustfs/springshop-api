package online.springshop.api.shared.idempotency;

import java.util.UUID;

public record IdempotencyKey (
    UUID value
)
{
    public IdempotencyKey
    {
        if (value == null)
            throw new NullPointerException("Idempotency key value must not be null");
    }
}
