package online.springshop.api.modules.messaging.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MessagingEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Spring Data operations for messaging event records. */
@Repository
public interface MessagingEventJpaRepository extends JpaRepository<MessagingEventEntity, UUID>
{
    /**
     * Inserts only when the id is absent, making the insert an atomic claim under concurrency.
     * @param entity event row to insert
     * @return one when inserted, zero when the id already exists
     */
    @Modifying
    @Query(value = """
    INSERT INTO messaging_events (id, status, created_at, updated_at)
    VALUES (:#{#entity?.id}, :#{#entity?.status.name()}, :#{#entity?.createdAt}, :#{#entity?.updatedAt})
    ON CONFLICT (id) DO NOTHING
    """,
    nativeQuery = true)
    int saveAndClaim (@Param("entity") MessagingEventEntity entity);

    /**
     * Loads an event while holding a pessimistic write lock on its row.
     * @param id UUID of the event
     * @return the locked event, or empty when no matching row exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT mee FROM MessagingEventEntity mee
    WHERE mee.id = :id
    """)
    Optional<MessagingEventEntity> findByIdAndLock (UUID id);

    /**
     * Deletes a bounded batch of old rows and skips rows locked by other transactions.
     * @param threshold creation times strictly before this instant are eligible
     * @param batchSize maximum number of rows to delete
     */
    @Modifying
    @Query(value = """
    DELETE FROM messaging_events WHERE id IN (
        SELECT id FROM messaging_events
        WHERE created_at < :threshold
        ORDER BY id, created_at
        FOR UPDATE SKIP LOCKED
        LIMIT :batchSize
    )
    """,
    nativeQuery = true)
    void deleteOldRecords (Instant threshold, int batchSize);
}
