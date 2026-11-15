package online.springshop.api.modules.messaging.application.port;

import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.MailMessage;
import online.springshop.api.shared.idempotency.IdempotencyKey;

/**
 * Application port for submitting email messages to an external mail provider.
 */
public interface MailSender
{
    /**
     * Submits a mail message for delivery.
     *
     * <p>The idempotency key identifies the submission so that retrying the same request does not
     * create a duplicate provider delivery.</p>
     *
     * @param mailMessage the message to submit
     * @param idempotencyKey the stable key identifying this submission
     * @return the identifier assigned by the mail provider
     * @throws NullPointerException when {@code mailMessage} or {@code idempotencyKey} is
     *     {@code null}
     */
    MailProviderId send (MailMessage mailMessage, IdempotencyKey idempotencyKey);
}
