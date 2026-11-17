package online.springshop.api.modules.messaging.domain.mail;

import com.fasterxml.uuid.Generators;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Tracks the provider-side delivery lifecycle of a submitted email.
 *
 * <p>A delivery is created after a mail provider accepts the email and is therefore initially
 * {@link MailDeliveryStatus#SUBMITTED}. Provider status events may skip intermediate states, arrive
 * more than once, or arrive out of order. {@link #updateStatus(MailDeliveryStatus, Instant, Clock)}
 * classifies those updates without allowing a terminal status to regress.</p>
 *
 * <p>Aggregate identity is defined exclusively by {@link MailDeliveryId}.</p>
 */
@Getter
public final class MailDelivery
{
    private final MailDeliveryId id;
    private final MailProviderId providerId;
    private MailDeliveryStatus status;

    private final Instant createdAt;
    private Instant statusChangedAt;
    private Instant providerStatusChangedAt;

    private MailDelivery (
        MailDeliveryId id,
        MailProviderId providerId,
        MailDeliveryStatus status,
        Instant createdAt,
        Instant statusChangedAt,
        Instant providerStatusChangedAt
    )
    {
        this.id = Objects.requireNonNull(id, "Mail delivery id must not be null");
        this.providerId = Objects.requireNonNull(providerId, "Provider id must not be null");
        this.status = Objects.requireNonNull(status, "Mail delivery status must not be null");

        this.createdAt = Objects.requireNonNull(createdAt, "Mail delivery created at must not be null");
        this.statusChangedAt = statusChangedAt;
        this.providerStatusChangedAt = providerStatusChangedAt;
    }

    /**
     * Creates a submitted mail delivery for a message accepted by the mail provider.
     *
     * @param providerId the identifier assigned by the mail provider
     * @param clock the clock used to record local creation time
     * @return a new delivery in the {@link MailDeliveryStatus#SUBMITTED} state
     * @throws NullPointerException when {@code providerId} or {@code clock} is {@code null}
     */
    public static MailDelivery from (
        MailProviderId providerId,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        MailDeliveryId id = new MailDeliveryId(Generators.timeBasedEpochGenerator().generate());
        MailDeliveryStatus status = MailDeliveryStatus.SUBMITTED;
        Instant createdAt = clock.instant();

        return new MailDelivery(
            id,
            providerId,
            status,
            createdAt,
            null,
            null
        );
    }

    /**
     * Restores a previously persisted mail delivery from trusted state.
     *
     * @param id the internal delivery identifier
     * @param providerId the identifier assigned by the mail provider
     * @param status the current delivery status
     * @param createdAt the local creation time
     * @param statusChangedAt the local time of the latest applied status change, or {@code null} for
     *     an unchanged submitted delivery
     * @param providerStatusChangedAt the provider occurrence time of the latest applied status
     *     event, or {@code null} when no provider status event has been applied
     * @return the restored mail delivery
     * @throws NullPointerException when {@code id}, {@code providerId}, {@code status}, or
     *     {@code createdAt} is {@code null}
     * @throws IllegalArgumentException when timestamps contradict the restored status or local
     *     creation time
     */
    public static MailDelivery restore (
        MailDeliveryId id,
        MailProviderId providerId,
        MailDeliveryStatus status,
        Instant createdAt,
        Instant statusChangedAt,
        Instant providerStatusChangedAt
    )
    {
        MailDelivery mailDelivery = new MailDelivery(
            id,
            providerId,
            status,
            createdAt,
            statusChangedAt,
            providerStatusChangedAt
        );

        if (statusChangedAt == null && status != MailDeliveryStatus.SUBMITTED)
            throw new IllegalArgumentException("Status changed at must not be null when status is different from SUBMITTED");

        if (statusChangedAt != null && statusChangedAt.isBefore(createdAt))
            throw new IllegalArgumentException("Status changed at must not be before created at");

        return mailDelivery;
    }

    /**
     * Applies a provider status event when it represents a valid forward transition.
     *
     * <p>An event older than the latest applied provider event is stale. Reapplying the current
     * status is duplicate. A transition that would regress or replace a terminal state is ignored.
     * Only an applied transition changes the aggregate and its timestamps.</p>
     *
     * @param newStatus the status reported by the provider
     * @param newProviderStatusChangedAt the time at which the provider reports the status occurred
     * @param clock the clock used to record when this application applies the transition
     * @return the classification of the attempted update
     * @throws NullPointerException when any argument is {@code null}
     * @throws IllegalArgumentException when the local update time precedes creation or the latest
     *     locally applied status change
     */
    public MailDeliveryUpdateResult updateStatus (
        MailDeliveryStatus newStatus,
        Instant newProviderStatusChangedAt,
        Clock clock
    )
    {
        if (newStatus == null)
            throw new NullPointerException("New status must not be null");

        if (newProviderStatusChangedAt == null)
            throw new NullPointerException("New provider status changed at must not be null");

        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (providerStatusChangedAt != null && newProviderStatusChangedAt.isBefore(providerStatusChangedAt))
            return MailDeliveryUpdateResult.STALE;

        if (newProviderStatusChangedAt.equals(providerStatusChangedAt) && status != newStatus)
            return MailDeliveryUpdateResult.CONFLICT;

        if (status == newStatus)
            return MailDeliveryUpdateResult.DUPLICATE;

        if (!canTransition(newStatus))
            return MailDeliveryUpdateResult.IGNORED_REGRESSION;

        Instant updateTime = clock.instant();
        validateUpdateTime(updateTime);

        this.status = newStatus;
        this.statusChangedAt = updateTime;
        this.providerStatusChangedAt = newProviderStatusChangedAt;

        return MailDeliveryUpdateResult.APPLIED;
    }

    /**
     * @return {@code boolean} whether the current status is terminal or not
     */
    public boolean isStatusTerminal ()
    {
        return switch (status)
        {
            case DELIVERED, BOUNCED, FAILED, SUPPRESSED -> true;
            case SUBMITTED, SENT, DELIVERY_DELAYED -> false;
        };
    }

    private boolean canTransition (MailDeliveryStatus newStatus)
    {
        return switch (this.status)
        {
            case DELIVERED, BOUNCED, FAILED, SUPPRESSED -> false;

            case SUBMITTED -> switch (newStatus)
            {
                case SENT, DELIVERY_DELAYED, DELIVERED, BOUNCED, FAILED, SUPPRESSED -> true;
                default -> false;
            };

            case SENT, DELIVERY_DELAYED -> switch (newStatus)
            {
                case DELIVERED, BOUNCED, FAILED -> true;
                default -> false;
            };
        };
    }

    private void validateUpdateTime (Instant updateTime)
    {
        if (updateTime.isBefore(createdAt))
            throw new IllegalArgumentException("Update time must not be before created at");

        if (statusChangedAt != null && updateTime.isBefore(statusChangedAt))
            throw new IllegalArgumentException("Update time must not be before status changed at");
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof MailDelivery that))
            return false;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode ()
    {
        return this.id.hashCode();
    }
}
