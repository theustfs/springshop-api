package online.springshop.api.modules.messaging.domain.mail.valueobject;

import lombok.Builder;

/**
 * Complete immutable content required to send an email. Construction rejects a null component.
 *
 * @param subject the non-null subject of the message
 * @param from the non-null sender address
 * @param recipient the non-null recipient address
 * @param body the non-null rendered HTML body
 */
@Builder
public record MailMessage (
    MailSubject subject,
    MailFrom from,
    MailRecipient recipient,
    HtmlBody body
)
{
    /**
     * Creates a complete mail message.
     *
     * @throws NullPointerException when any component is {@code null}
     */
    public MailMessage
    {
        if (subject == null)
            throw new NullPointerException("Mail message subject must not be null");

        if (from == null)
            throw new NullPointerException("Mail message from must not be null");

        if (recipient == null)
            throw new NullPointerException("Mail message recipient must not be null");

        if (body == null)
            throw new NullPointerException("Mail message body must not be null");
    }
}
