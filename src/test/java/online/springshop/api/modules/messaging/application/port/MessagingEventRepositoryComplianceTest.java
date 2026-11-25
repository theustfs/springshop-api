package online.springshop.api.modules.messaging.application.port;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import online.springshop.api.modules.messaging.domain.event.MessagingEvent;
import online.springshop.api.modules.messaging.domain.event.MessagingEventStatus;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MessagingEventEntity;
import online.springshop.api.modules.messaging.infrastructure.persistence.mapper.MessagingEventMapperImpl;
import online.springshop.api.modules.messaging.infrastructure.persistence.repository.MessagingEventRepositoryJpaAdapter;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import online.springshop.api.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    MessagingEventMapperImpl.class,
    MessagingEventRepositoryJpaAdapter.class
})
@DisplayName("Messaging event repository compliance tests")
class MessagingEventRepositoryComplianceTest extends AbstractDataJpaTest
{
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MessagingEventRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase ()
    {
        entityManager.createQuery("DELETE FROM MessagingEventEntity").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private MessagingEvent createEvent ()
    {
        return MessagingEvent.from(
            new IdempotencyKey(Generators.timeBasedEpochGenerator().generate()),
            Clock.fixed(CREATED_AT, ZoneOffset.UTC)
        );
    }

    private void saveInSeparateTransaction (MessagingEvent event)
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> repository.save(event));
    }

    private MessagingEventEntity acquireLock (IdempotencyKey id)
    {
        return entityManager.find(
            MessagingEventEntity.class,
            id.value(),
            LockModeType.PESSIMISTIC_WRITE,
            Map.of("jakarta.persistence.lock.timeout", 0)
        );
    }

    private void assertSearchAndLock (
        MessagingEvent event,
        Consumer<MessagingEvent> searchFunction
    ) throws Exception
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        Future<?> lockFuture = executor.submit(() -> {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            transaction.executeWithoutResult(status -> {
                searchFunction.accept(event);
                lockAcquired.countDown();
                try
                {
                    if (!releaseLock.await(5, TimeUnit.SECONDS))
                        throw new AssertionError("Timeout waiting to release lock");
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
        });

        try
        {
            assertTrue(lockAcquired.await(5, TimeUnit.SECONDS));
            assertThrows(LockTimeoutException.class, () -> {
                TransactionTemplate transaction = new TransactionTemplate(transactionManager);
                transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                transaction.executeWithoutResult(status -> acquireLock(event.getId()));
            });
        }
        finally
        {
            releaseLock.countDown();
            try
            {
                lockFuture.get(5, TimeUnit.SECONDS);
            }
            finally
            {
                executor.shutdownNow();
            }
        }

        assertTrue(repository.findById(event.getId()).isPresent());
        assertNotNull(acquireLock(event.getId()));
    }

    @Nested
    @DisplayName("Claim tests")
    class ClaimTests
    {
        @Test
        @DisplayName("Should claim a new event and reject a duplicate claim")
        void shouldClaimNewEventAndRejectDuplicateClaim ()
        {
            IdempotencyKey id = new IdempotencyKey(Generators.timeBasedEpochGenerator().generate());
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            MessagingEvent firstClaim = MessagingEvent.restore(id, MessagingEventStatus.CREATED, createdAt, null);
            MessagingEvent duplicateClaim = MessagingEvent.restore(
                id,
                MessagingEventStatus.CREATED,
                createdAt.plusSeconds(1),
                null
            );

            assertTrue(repository.saveAndClaim(firstClaim));
            assertFalse(repository.saveAndClaim(duplicateClaim));

            MessagingEvent persisted = repository.findById(id).orElseThrow();
            assertEquals(MessagingEventStatus.CREATED, persisted.getStatus());
            assertEquals(createdAt, persisted.getCreatedAt());
        }

        @Test
        @DisplayName("Should allow only one concurrent claim for an event id")
        void shouldAllowOnlyOneConcurrentClaim () throws Exception
        {
            IdempotencyKey id = new IdempotencyKey(Generators.timeBasedEpochGenerator().generate());
            MessagingEvent first = MessagingEvent.restore(id, MessagingEventStatus.CREATED, CREATED_AT, null);
            MessagingEvent second = MessagingEvent.restore(id, MessagingEventStatus.CREATED, CREATED_AT.plusSeconds(1), null);
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Callable<Boolean> claimFirst = () -> claimWhenReleased(start, first);
            Callable<Boolean> claimSecond = () -> claimWhenReleased(start, second);
            Future<Boolean> firstResult = executor.submit(claimFirst);
            Future<Boolean> secondResult = executor.submit(claimSecond);
            start.countDown();

            try
            {
                assertNotEquals(firstResult.get(5, TimeUnit.SECONDS), secondResult.get(5, TimeUnit.SECONDS));
            }
            finally
            {
                executor.shutdownNow();
            }
        }

        private boolean claimWhenReleased (CountDownLatch start, MessagingEvent event) throws Exception
        {
            if (!start.await(5, TimeUnit.SECONDS))
                throw new AssertionError("Timeout waiting to start claim");

            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return Boolean.TRUE.equals(transaction.execute(status -> repository.saveAndClaim(event)));
        }
    }

    @Nested
    @DisplayName("Persistence tests")
    class PersistenceTests
    {
        @Test
        @DisplayName("Should persist and update an event")
        void shouldPersistAndUpdateEvent ()
        {
            MessagingEvent event = createEvent();
            repository.save(event);
            event.updateStatus(MessagingEventStatus.PROCESSING, Clock.fixed(CREATED_AT.plusSeconds(5), ZoneOffset.UTC));
            repository.save(event);

            MessagingEvent persisted = repository.findById(event.getId()).orElseThrow();
            assertEquals(event.getId(), persisted.getId());
            assertEquals(MessagingEventStatus.PROCESSING, persisted.getStatus());
            assertEquals(CREATED_AT, persisted.getCreatedAt());
            assertEquals(CREATED_AT.plusSeconds(5), persisted.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return empty when an event does not exist")
        void shouldReturnEmptyForMissingEvent ()
        {
            assertTrue(repository.findById(new IdempotencyKey(UUID.randomUUID())).isEmpty());
        }

        @Test
        @DisplayName("Should find and lock an event row")
        void shouldFindAndLockEventRow () throws Exception
        {
            MessagingEvent event = createEvent();
            saveInSeparateTransaction(event);
            assertSearchAndLock(event, lockedEvent -> repository.findByIdAndLock(lockedEvent.getId()).orElseThrow());
        }

        @Test
        @DisplayName("Should return empty when a locked event does not exist")
        void shouldReturnEmptyForMissingLockedEvent ()
        {
            assertTrue(repository.findByIdAndLock(new IdempotencyKey(UUID.randomUUID())).isEmpty());
        }

        @Test
        @DisplayName("Should delete only events older than the threshold up to the batch size")
        void shouldDeleteOldEventsWithinBatchSize ()
        {
            MessagingEvent oldFirst = MessagingEvent.restore(
                new IdempotencyKey(UUID.randomUUID()), MessagingEventStatus.PROCESSED, CREATED_AT, null
            );
            MessagingEvent oldSecond = MessagingEvent.restore(
                new IdempotencyKey(UUID.randomUUID()), MessagingEventStatus.FAILED, CREATED_AT.plusSeconds(1), null
            );
            MessagingEvent atThreshold = MessagingEvent.restore(
                new IdempotencyKey(UUID.randomUUID()), MessagingEventStatus.PROCESSED, CREATED_AT.plusSeconds(10), null
            );
            saveInSeparateTransaction(oldFirst);
            saveInSeparateTransaction(oldSecond);
            saveInSeparateTransaction(atThreshold);

            repository.deleteOldRecords(CREATED_AT.plusSeconds(10), 1);

            int remainingCount = (repository.findById(oldFirst.getId()).isPresent() ? 1 : 0)
                + (repository.findById(oldSecond.getId()).isPresent() ? 1 : 0)
                + (repository.findById(atThreshold.getId()).isPresent() ? 1 : 0);
            assertEquals(2, remainingCount);
            assertTrue(repository.findById(atThreshold.getId()).isPresent());
        }
    }
}
