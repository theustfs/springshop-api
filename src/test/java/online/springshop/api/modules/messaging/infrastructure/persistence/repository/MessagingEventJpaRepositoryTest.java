package online.springshop.api.modules.messaging.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import online.springshop.api.modules.messaging.domain.event.MessagingEventStatus;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MessagingEventEntity;
import online.springshop.api.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Messaging event JPA repository tests")
class MessagingEventJpaRepositoryTest extends AbstractDataJpaTest
{
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MessagingEventJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private MessagingEventEntity createEntity ()
    {
        return new MessagingEventEntity(UUID.randomUUID(), MessagingEventStatus.CREATED, CREATED_AT, null);
    }

    private void saveInSeparateTransaction (MessagingEventEntity entity)
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> repository.save(entity));
    }

    private MessagingEventEntity acquireLock (UUID id)
    {
        return entityManager.find(
            MessagingEventEntity.class,
            id,
            LockModeType.PESSIMISTIC_WRITE,
            Map.of("jakarta.persistence.lock.timeout", 0)
        );
    }

    @Nested
    @DisplayName("Batch cleanup tests")
    class BatchCleanupTests
    {
        @Test
        @DisplayName("Should skip a locked old row during batch cleanup")
        void shouldSkipLockedRowDuringCleanup () throws Exception
        {
            MessagingEventEntity entity = createEntity();
            saveInSeparateTransaction(entity);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            CountDownLatch lockAcquired = new CountDownLatch(1);
            CountDownLatch releaseLock = new CountDownLatch(1);

            Future<?> lockFuture = executor.submit(() -> {
                TransactionTemplate transaction = new TransactionTemplate(transactionManager);
                transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transaction.executeWithoutResult(status -> {
                    assertNotNull(acquireLock(entity.getId()));
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
                TransactionTemplate cleanupTransaction = new TransactionTemplate(transactionManager);
                cleanupTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                assertDoesNotThrow(() -> cleanupTransaction.executeWithoutResult(status ->
                    repository.deleteOldRecords(CREATED_AT.plusSeconds(1), 10)
                ));
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

            TransactionTemplate verifyTransaction = new TransactionTemplate(transactionManager);
            verifyTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            Boolean execute = verifyTransaction.execute(status -> repository.existsById(entity.getId()));
            assertNotNull(execute);
            assertTrue(execute);
        }
    }
}
