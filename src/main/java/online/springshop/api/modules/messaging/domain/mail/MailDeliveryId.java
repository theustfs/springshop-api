package online.springshop.api.modules.messaging.domain.mail;

import java.util.UUID;

/**
 * Internal identity of a mail-delivery aggregate.
 *
 * @param value the non-null UUID value
 */
public record MailDeliveryId (
    UUID value
)
{
    /**
     * Creates a mail-delivery identifier.
     *
     * @param value the non-null UUID value
     * @throws NullPointerException when {@code value} is {@code null}
     */
    public MailDeliveryId
    {
        if (value == null)
            throw new NullPointerException("Mail delivery id must not be null");
    }
}
