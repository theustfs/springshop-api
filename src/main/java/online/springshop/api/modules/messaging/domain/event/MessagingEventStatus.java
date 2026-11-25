package online.springshop.api.modules.messaging.domain.event;

/** Lifecycle state recorded while a messaging event is handled. */
public enum MessagingEventStatus
{
    /** Event was recorded and is awaiting processing. */
    CREATED,
    /** A consumer has claimed the event and is processing it. */
    PROCESSING,
    /** Processing completed successfully. */
    PROCESSED,
    /** Processing failed. */
    FAILED
}
