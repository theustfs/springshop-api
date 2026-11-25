package online.springshop.api.modules.messaging.application.port;

import online.springshop.api.modules.messaging.domain.event.MessagingEvent;
import online.springshop.api.shared.idempotency.IdempotencyKey;

import java.time.Instant;
import java.util.Optional;

public interface MessagingEventRepository
{
    /**
     * persists the messaging event
     * @param messagingEvent the messaging event to persist
     */
    void save (MessagingEvent messagingEvent);

    /**
     * tries to persist a messaging event and claim it
     * @param messagingEvent the messaging event to persist
     * @return {@code true} if persisted and claimed successfully, {@code false} otherwise
     */
    boolean saveAndClaim (MessagingEvent messagingEvent);

    /**
     * finds a messaging event by its id
     * @param id messaging event id
     * @return {@code Optinal<MessagingEvent>} containing the messaging event, or empty if it does not exist
     */
    Optional<MessagingEvent> findById (IdempotencyKey id);

    /**
     * finds a messaging event by its id and lock is row on the database
     * @param id messaging event id
     * @return {@code Optinal<MessagingEvent>} containing the messaging event, or empty if it does not exist
     */
    Optional<MessagingEvent> findByIdAndLock (IdempotencyKey id);

    /**
     * deletes old records
     * @param threshold instant threshold, records created before the threshold will be eligible for deletion
     * @param batchSize maximum number of rows affected by each iteration
     */
    void deleteOldRecords (Instant threshold, int batchSize);
}
