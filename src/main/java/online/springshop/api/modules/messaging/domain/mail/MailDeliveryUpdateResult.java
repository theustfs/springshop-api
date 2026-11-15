package online.springshop.api.modules.messaging.domain.mail;

/**
 * Classification produced when a provider status event is applied to a mail delivery.
 */
public enum MailDeliveryUpdateResult
{
    /** The event predates the latest provider status event already applied. */
    STALE,

    /** The event has the latest provider timestamp but reports a different status. */
    CONFLICT,

    /** The event reports the delivery's current status again. */
    DUPLICATE,

    /** The event would regress the lifecycle or replace a terminal outcome. */
    IGNORED_REGRESSION,

    /** The event produced a valid state transition and updated the aggregate. */
    APPLIED
}
