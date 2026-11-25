package online.springshop.api.modules.messaging.domain.event;

import lombok.Getter;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** Records the processing lifecycle of a message, identified by its idempotency key. */
@Getter
public final class MessagingEvent
{
    private final IdempotencyKey id;
    private MessagingEventStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private MessagingEvent (
        IdempotencyKey id,
        MessagingEventStatus status,
        Instant createdAt,
        Instant updatedAt
    )
    {
        this.id = Objects.requireNonNull(id, "Messaging event id must not be null");
        this.status = Objects.requireNonNull(status, "Messaging event status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Messaging event created at must not be null");
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new event in {@link MessagingEventStatus#CREATED} at the clock's current instant.
     * @param id stable key used to identify duplicate deliveries
     * @param clock source of the creation time
     * @return a newly created event
     * @throws NullPointerException if the id or clock is {@code null}
     */
    public static MessagingEvent from (
        IdempotencyKey id,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (id == null)
            throw new NullPointerException("Id must not be null");

        MessagingEventStatus status = MessagingEventStatus.CREATED;
        Instant createdAt = clock.instant();

        return new MessagingEvent(id, status, createdAt, null);
    }

    /**
     * Reconstructs an event from persisted state without changing its timestamps.
     * @param id stable event identifier
     * @param status persisted lifecycle status
     * @param createdAt original creation time
     * @param updatedAt most recent status update time, or {@code null} if never updated
     * @return the restored event
     * @throws NullPointerException if either {@code id}, {@code status} or {@code createdAt} are null
     * @throws IllegalArgumentException if {@code updatedAt} is before {@code createdAt}
     */
    public static MessagingEvent restore (
        IdempotencyKey id,
        MessagingEventStatus status,
        Instant createdAt,
        Instant updatedAt
    )
    {
        if (updatedAt != null && updatedAt.isBefore(createdAt))
            throw new IllegalArgumentException("Updated at must not be before created at");

        return new MessagingEvent(id, status, createdAt, updatedAt);
    }

    /**
     * Changes the status and records the clock's current instant. Timestamp ordering is checked,
     * but status transitions are not restricted here.
     * @param newStatus status to record
     * @param clock source of the update time
     * @throws NullPointerException if the status or clock is {@code null}
     * @throws IllegalArgumentException if the clock time precedes creation or the previous update
     */
    public void updateStatus (
        MessagingEventStatus newStatus,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (newStatus == null)
            throw new NullPointerException("New status must not be null for update");

        Instant updateTime = clock.instant();

        if (updateTime.isBefore(createdAt))
            throw new IllegalArgumentException("Update time must not be before created at");

        if (updatedAt != null && updateTime.isBefore(updatedAt))
            throw new IllegalArgumentException("Updated time must not be before updatedAt");

        status = newStatus;
        updatedAt = updateTime;
    }
}
