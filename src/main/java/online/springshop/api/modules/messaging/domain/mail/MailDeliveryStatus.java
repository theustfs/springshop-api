package online.springshop.api.modules.messaging.domain.mail;

/**
 * Provider-side lifecycle status of a submitted email.
 */
public enum MailDeliveryStatus
{
    /** The provider accepted the email for processing. */
    SUBMITTED,

    /** The provider sent the email to the recipient's mail server. */
    SENT,

    /** The recipient's mail server accepted the email. */
    DELIVERED,

    /** A temporary condition postponed delivery and the outcome is not yet final. */
    DELIVERY_DELAYED,

    /** The provider skipped delivery because the recipient is suppressed. */
    SUPPRESSED,

    /** The recipient's mail server rejected or returned the email. */
    BOUNCED,

    /** The provider could not complete delivery. */
    FAILED
}
