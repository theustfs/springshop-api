package online.springshop.api.modules.messaging.domain.mail;

import java.util.UUID;

/**
 * Identifier assigned to an email by the external mail provider.
 *
 * @param value the non-null provider UUID
 */
public record MailProviderId (
    UUID value
)
{
    /**
     * Creates a mail-provider identifier.
     *
     * @param value the non-null provider UUID
     * @throws NullPointerException when {@code value} is {@code null}
     */
    public MailProviderId
    {
        if (value == null)
            throw new NullPointerException("Mail provider id must not be null");
    }
}
