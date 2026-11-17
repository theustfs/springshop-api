package online.springshop.api.modules.messaging.application.port;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryId;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryStatus;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MailDeliveryEntity;
import online.springshop.api.modules.messaging.infrastructure.persistence.mapper.MailDeliveryMapperImpl;
import online.springshop.api.modules.messaging.infrastructure.persistence.repository.MailDeliveryRepositoryJpaAdapter;
import online.springshop.api.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    MailDeliveryRepositoryJpaAdapter.class,
    MailDeliveryMapperImpl.class
})
@DisplayName("Mail delivery repository compliance tests")
class MailDeliveryRepositoryComplianceTest extends AbstractDataJpaTest
{
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MailDeliveryRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase ()
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM MailDeliveryEntity").executeUpdate();
            entityManager.clear();
        });
    }

    private MailDelivery createMailDelivery ()
    {
        return MailDelivery.from(
            new MailProviderId(Generators.timeBasedEpochGenerator().generate()),
            Clock.fixed(CREATED_AT, ZoneOffset.UTC)
        );
    }

    private MailDeliveryEntity acquireLock (MailDeliveryId id)
    {
        return entityManager.find(
            MailDeliveryEntity.class,
            id.value(),
            LockModeType.PESSIMISTIC_WRITE,
            Map.of("jakarta.persistence.lock.timeout", 0)
        );
    }

    private void saveInSeparateTransaction (MailDelivery mailDelivery)
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transaction.executeWithoutResult(status -> repository.save(mailDelivery));
    }

    private void assertSearchAndLock (
        MailDelivery mailDelivery,
        Consumer<MailDelivery> searchFunction
    ) throws Exception
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        Future<?> lockFuture = executor.submit(() -> {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            transaction.executeWithoutResult(status -> {
                searchFunction.accept(mailDelivery);
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
            assertThrows(
                LockTimeoutException.class,
                () -> {
                    TransactionTemplate transaction = new TransactionTemplate(transactionManager);
                    transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                    transaction.executeWithoutResult(s -> acquireLock(mailDelivery.getId()));
                }
            );
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

        assertTrue(repository.findById(mailDelivery.getId()).isPresent());
        assertNotNull(
            assertDoesNotThrow(() -> acquireLock(mailDelivery.getId())),
            "Could not acquire row lock"
        );
    }

    private void assertDeleteSkipLocked (
        MailDelivery mailDelivery,
        Runnable deleteFunction
    ) throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        Future<?> lockFuture = executor.submit(() -> {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            transaction.executeWithoutResult(status -> {
                MailDeliveryEntity locked = acquireLock(mailDelivery.getId());
                assertNotNull(locked, "Could not find mail delivery to acquire lock");

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
            if (!lockAcquired.await(5, TimeUnit.SECONDS))
            {
                lockFuture.get(1, TimeUnit.SECONDS);
                throw new AssertionError("Timeout waiting to acquire lock");
            }

            Future<?> deleteFuture = executor.submit(() -> {
                TransactionTemplate transaction = new TransactionTemplate(transactionManager);
                transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                transaction.executeWithoutResult(status -> deleteFunction.run());
            });
            deleteFuture.get(5, TimeUnit.SECONDS);
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
    }

    @Nested
    @DisplayName("Save tests")
    class SaveTests
    {
        @Test
        @DisplayName("Should save mail delivery")
        void shouldSaveMailDelivery ()
        {
            MailDelivery mailDelivery = createMailDelivery();
            repository.save(mailDelivery);

            MailDelivery saved = repository.findById(mailDelivery.getId()).orElseThrow();

            assertEquals(mailDelivery.getId(), saved.getId());
            assertEquals(mailDelivery.getProviderId(), saved.getProviderId());
            assertEquals(mailDelivery.getStatus(), saved.getStatus());
            assertEquals(mailDelivery.getCreatedAt(), saved.getCreatedAt());
            assertEquals(mailDelivery.getStatusChangedAt(), saved.getStatusChangedAt());
            assertEquals(mailDelivery.getProviderStatusChangedAt(), saved.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should update mail delivery")
        void shouldUpdateMailDelivery ()
        {
            MailDelivery mailDelivery = createMailDelivery();

            repository.save(mailDelivery);

            MailDelivery saved = repository.findById(mailDelivery.getId()).orElseThrow();
            Instant providerChangedAt = CREATED_AT.plusSeconds(30);
            saved.updateStatus(
                MailDeliveryStatus.DELIVERED,
                providerChangedAt,
                Clock.fixed(CREATED_AT.plusSeconds(60), ZoneOffset.UTC)
            );

            repository.save(saved);
            MailDelivery updated = repository.findById(mailDelivery.getId()).orElseThrow();

            assertEquals(MailDeliveryStatus.DELIVERED, updated.getStatus());
            assertEquals(CREATED_AT.plusSeconds(60), updated.getStatusChangedAt());
            assertEquals(providerChangedAt, updated.getProviderStatusChangedAt());
        }
    }

    @Nested
    @DisplayName("Find by id tests")
    class FindByIdTests
    {
        @Test
        @DisplayName("Should find mail delivery")
        void shouldFindMailDelivery ()
        {
            MailDelivery mailDelivery = createMailDelivery();
            repository.save(mailDelivery);

            assertEquals(mailDelivery.getId(), repository.findById(mailDelivery.getId()).orElseThrow().getId());
        }

        @Test
        @DisplayName("Should return empty when mail delivery does not exist")
        void shouldReturnEmptyWhenMailDeliveryDoesNotExist ()
        {
            assertTrue(repository.findById(new MailDeliveryId(UUID.randomUUID())).isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by provider id tests")
    class FindByProviderIdTests
    {
        @Test
        @DisplayName("Should find mail delivery")
        void shouldFindMailDelivery ()
        {
            MailDelivery mailDelivery = createMailDelivery();
            repository.save(mailDelivery);

            assertEquals(mailDelivery.getId(), repository.findByProviderId(mailDelivery.getProviderId()).orElseThrow().getId());
        }

        @Test
        @DisplayName("Should return empty when provider id does not exist")
        void shouldReturnEmptyWhenProviderIdDoesNotExist ()
        {
            assertTrue(repository.findByProviderId(new MailProviderId(UUID.randomUUID())).isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by id and lock tests")
    class FindByIdAndLockTests
    {
        @Test
        @DisplayName("Should find and lock mail delivery row")
        void shouldFindAndLockMailDeliveryRow () throws Exception
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            assertSearchAndLock(
                mailDelivery,
                delivery -> repository.findByIdAndLock(delivery.getId()).orElseThrow()
            );
        }

        @Test
        @DisplayName("Should return empty when mail delivery does not exist")
        void shouldReturnEmptyWhenMailDeliveryDoesNotExist ()
        {
            assertTrue(repository.findByIdAndLock(new MailDeliveryId(UUID.randomUUID())).isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by provider id and lock tests")
    class FindByProviderIdAndLockTests
    {
        @Test
        @DisplayName("Should find and lock mail delivery row")
        void shouldFindAndLockMailDeliveryRow () throws Exception
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            assertSearchAndLock(
                mailDelivery,
                delivery -> repository.findByProviderIdAndLock(delivery.getProviderId()).orElseThrow()
            );
        }

        @Test
        @DisplayName("Should return empty when provider id does not exist")
        void shouldReturnEmptyWhenProviderIdDoesNotExist ()
        {
            assertTrue(repository.findByProviderIdAndLock(new MailProviderId(UUID.randomUUID())).isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete tests")
    class DeleteTests
    {
        @Test
        @DisplayName("Should delete by mail delivery id")
        void shouldDeleteByMailDeliveryId ()
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            repository.delete(mailDelivery.getId());

            assertTrue(repository.findById(mailDelivery.getId()).isEmpty());
        }

        @Test
        @DisplayName("Should skip locked row when deleting by mail delivery id")
        void shouldSkipLockedRowWhenDeletingByMailDeliveryId () throws Exception
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            assertDeleteSkipLocked(mailDelivery, () -> repository.delete(mailDelivery.getId()));

            assertTrue(repository.findById(mailDelivery.getId()).isPresent());
        }

        @Test
        @DisplayName("Should delete by provider id")
        void shouldDeleteByProviderId ()
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            repository.delete(mailDelivery.getProviderId());

            assertTrue(repository.findByProviderId(mailDelivery.getProviderId()).isEmpty());
        }

        @Test
        @DisplayName("Should skip locked row when deleting by provider id")
        void shouldSkipLockedRowWhenDeletingByProviderId () throws Exception
        {
            MailDelivery mailDelivery = createMailDelivery();
            saveInSeparateTransaction(mailDelivery);

            assertDeleteSkipLocked(mailDelivery, () -> repository.delete(mailDelivery.getProviderId()));

            assertTrue(repository.findByProviderId(mailDelivery.getProviderId()).isPresent());
        }
    }
}
