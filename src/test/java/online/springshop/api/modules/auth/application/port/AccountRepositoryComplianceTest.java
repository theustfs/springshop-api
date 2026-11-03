package online.springshop.api.modules.auth.application.port;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.Role;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.mapper.AccountMapperImpl;
import online.springshop.api.modules.auth.infrastructure.persistence.repository.AccountRepositoryJpaAdapter;
import online.springshop.api.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    AccountRepositoryJpaAdapter.class,
    AccountMapperImpl.class
})
class AccountRepositoryComplianceTest extends AbstractDataJpaTest
{
    @Autowired
    private AccountRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private void cleanDatabase ()
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transaction.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM AccountEntity")
                .executeUpdate();

            entityManager.clear();
        });
    }

    private AccountEntity acquireLock (UUID id)
    {
        TransactionTemplate accountTransaction = new TransactionTemplate(transactionManager);
        accountTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return accountTransaction.execute(status -> entityManager.find(
            AccountEntity.class,
            id,
            LockModeType.PESSIMISTIC_WRITE,
            Map.of("jakarta.persistence.lock.timeout", 0)
        ));
    }

    private Account getAccount ()
    {
        Instant now = Instant.now();

        return Account.restore(
            Generators.timeBasedEpochGenerator().generate(),
            Email.restore("test%s@example.com".formatted((int) (Math.random() * Integer.MAX_VALUE))),
            EncodedPassword.from("encoded_password"),
            Role.PRODUCT_MANAGER,
            now.minusSeconds(60),
            now.minusSeconds(30),
            now.minusSeconds(30),
            true
        );
    }

    private void saveAccountInSeparateTransaction (Account account)
    {
        TransactionTemplate accountTransaction = new TransactionTemplate(transactionManager);
        accountTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        accountTransaction.executeWithoutResult(status -> repository.save(account));
    }

    private void saveAccountsInSeparateTransaction (List<Account> accounts)
    {
        TransactionTemplate accountTransaction = new TransactionTemplate(transactionManager);
        accountTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        accountTransaction.executeWithoutResult(status -> {
            for (Account account : accounts)
                repository.save(account);
        });
    }

    private void assertSearchAndLock (
        Account account,
        Consumer<Account> lockFunction
    ) throws Exception
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch lockLatch = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        Future<?> lockFuture = executorService.submit(() ->
        {
            TransactionTemplate lockTransaction = new TransactionTemplate(transactionManager);
            lockTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            lockTransaction.executeWithoutResult(status ->
            {
                // execute the function that executes the lock
                lockFunction.accept(account);

                lockLatch.countDown();

                try
                {
                    if (!releaseLatch.await(5, TimeUnit.SECONDS))
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
            assertTrue(lockLatch.await(5, TimeUnit.SECONDS));

            assertThrows(
                LockTimeoutException.class,
                () -> acquireLock(account.getId())
            );
        }
        finally
        {
            releaseLatch.countDown();

            try
            {
                lockFuture.get(5, TimeUnit.SECONDS);
            }
            finally
            {
                executorService.shutdownNow();
            }
        }

        assertTrue(repository.existsById(account.getId()));

        // assert row is not locked after transaction ends
        AccountEntity entity = assertDoesNotThrow(() -> acquireLock(account.getId()));
        assertNotNull(entity);
    }

    private void assertLockAndDelete (
        List<Account> accountsToDelete,
        Instant threshold,
        Integer batchSize,
        BiConsumer<Instant, Integer> lockFunction
    ) throws Exception
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CountDownLatch lockLatch = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        Future<?> lockFuture = executorService.submit(() ->
        {
            TransactionTemplate lockTransaction = new TransactionTemplate(transactionManager);
            lockTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            lockTransaction.executeWithoutResult(status ->
            {
                // execute the function that executes the lock
                lockFunction.accept(threshold, batchSize);

                lockLatch.countDown();

                try
                {
                    if (!releaseLatch.await(5, TimeUnit.SECONDS))
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
            assertTrue(lockLatch.await(5, TimeUnit.SECONDS));

            assertThrows(
                LockTimeoutException.class,
                () ->
                {
                    for (Account account : accountsToDelete)
                        acquireLock(account.getId());
                }
            );
        }
        finally
        {
            releaseLatch.countDown();

            try
            {
                lockFuture.get(5, TimeUnit.SECONDS);
            }
            finally
            {
                executorService.shutdownNow();
            }
        }
    }

    private void assertDeleteSkipLocked (
        Account accountToLock,
        Consumer<Account> lockFunction,
        BiConsumer<Instant, Integer> deleteFunction,
        Instant threshold,
        Integer batchSize
    ) throws Exception
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch lockLatch = new CountDownLatch(1);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        Future<?> lockFuture = executorService.submit(() ->
        {
            TransactionTemplate lockTransaction = new TransactionTemplate(transactionManager);
            lockTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            lockTransaction.executeWithoutResult(status ->
            {
                // execute the function that executes the lock
                lockFunction.accept(accountToLock);

                lockLatch.countDown();

                try
                {
                    if (!releaseLatch.await(5, TimeUnit.SECONDS))
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
            if (!lockLatch.await(5, TimeUnit.SECONDS))
            {
                lockFuture.get(1, TimeUnit.SECONDS);
                throw new AssertionError("Timeout waiting to release lock");
            }

            Future<?> deleteFuture = executorService.submit(() ->
            {
                TransactionTemplate deleteTransaction = new TransactionTemplate(transactionManager);
                deleteTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                deleteTransaction.executeWithoutResult(status ->
                {
                    deleteFunction.accept(threshold, batchSize);
                });
            });

            deleteFuture.get(5, TimeUnit.SECONDS);
        }
        finally
        {
            releaseLatch.countDown();

            try
            {
                lockFuture.get(5, TimeUnit.SECONDS);
            }
            finally
            {
                executorService.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("Save tests")
    class SaveTests
    {
        @Test
        @DisplayName("Should save account")
        void shouldSaveAccount ()
        {
            Account account = getAccount();
            repository.save(account);

            Account savedAccount = repository.findById(account.getId()).orElseThrow();

            assertEquals(account.getId(), savedAccount.getId());
            assertEquals(account.getEmail(), savedAccount.getEmail());
            assertEquals(account.getEncodedPassword(), savedAccount.getEncodedPassword());
            assertEquals(account.getRole(), savedAccount.getRole());
            assertEquals(account.getCreatedAt(), savedAccount.getCreatedAt());
            assertEquals(account.getUpdatedAt(), savedAccount.getUpdatedAt());
            assertEquals(account.getDeletionScheduledAt(), savedAccount.getDeletionScheduledAt());
            assertEquals(account.isVerified(), savedAccount.isVerified());
        }
    }

    @Nested
    @DisplayName("Find by id tests")
    class FindByIdTests
    {
        @Test
        @DisplayName("Should find account")
        void shouldFindAccount ()
        {
            Account account = getAccount();
            repository.save(account);

            Account savedAccount = repository.findById(account.getId()).orElseThrow();

            assertEquals(account.getId(), savedAccount.getId());
        }

        @Test
        @DisplayName("Should return empty when account does not exist")
        void shouldReturnEmptyWhenAccountDoesNotExist ()
        {
            Optional<Account> account = repository.findById(Generators.timeBasedEpochGenerator().generate());

            assertTrue(account.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by email tests")
    class FindByEmailTests
    {
        @Test
        @DisplayName("Should find account")
        void shouldFindAccount ()
        {
            Account account = getAccount();
            repository.save(account);

            Account savedAccount = repository.findByEmail(account.getEmail()).orElseThrow();

            assertEquals(account.getEmail(), savedAccount.getEmail());
        }

        @Test
        @DisplayName("Should return empty when account does not exist")
        void shouldReturnEmptyWhenAccountDoesNotExist ()
        {
            Optional<Account> account = repository.findByEmail(Email.restore("wrong_email@example.com"));

            assertTrue(account.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by id and lock tests")
    class FindByIdAndLockTests
    {
        @Test
        @DisplayName("Should find and lock account row")
        void shouldFindAndLockAccountRow() throws Exception
        {
            Account account = getAccount();
            saveAccountInSeparateTransaction(account);

            Consumer<Account> function = a -> repository.findByIdAndLock(a.getId()).orElseThrow();

            assertSearchAndLock(account, function);

            cleanDatabase();
        }

        @Test
        @DisplayName("Should return empty when account does not exist")
        void shouldReturnEmptyWhenAccountDoesNotExist ()
        {
            Optional<Account> account = repository.findByIdAndLock(Generators.timeBasedEpochGenerator().generate());

            assertTrue(account.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by email and lock tests")
    class FindByEmailAndLockTests
    {
        @Test
        @DisplayName("Should find and lock account row")
        void shouldFindAndLockAccountRow () throws Exception
        {
            Account account = getAccount();
            saveAccountInSeparateTransaction(account);

            Consumer<Account> function = a -> repository.findByEmailAndLock(a.getEmail()).orElseThrow();

            assertSearchAndLock(account, function);

            cleanDatabase();
        }

        @Test
        @DisplayName("Should return empty when account does not exist")
        void shouldReturnEmptyWhenAccountDoesNotExist ()
        {
            Optional<Account> account = repository.findByEmailAndLock(Email.restore("random@example.com"));

            assertTrue(account.isEmpty());
        }
    }

    @Nested
    @DisplayName("Exist by id tests")
    class ExistByIdTests
    {
        @Test
        @DisplayName("Should report true when account exists")
        void shouldReportTrueWhenAccountExists ()
        {
            Account account = getAccount();
            repository.save(account);

            assertTrue(repository.existsById(account.getId()));
        }

        @Test
        @DisplayName("Should report false when account does not exist")
        void shouldReportFalseWhenAccountDoesNotExist ()
        {
            assertFalse(repository.existsById(Generators.timeBasedEpochGenerator().generate()));
        }
    }

    @Nested
    @DisplayName("Exist by email tests")
    class ExistByEmailTests
    {
        @Test
        @DisplayName("Should report true when account exists")
        void shouldReportTrueWhenAccountExists ()
        {
            Account account = getAccount();
            repository.save(account);

            assertTrue(repository.existsByEmail(account.getEmail()));
        }

        @Test
        @DisplayName("Should report false when account does not exist")
        void shouldReportFalseWhenAccountDoesNotExist ()
        {
            assertFalse(repository.existsByEmail(Email.restore("random@example.com")));
        }
    }

    @Nested
    @DisplayName("Delete unverified accounts tests")
    class DeleteUnverifiedAccountsTests
    {
        private Account getUnverifiedAccount (Instant createdAt)
        {
            return Account.restore(
                Generators.timeBasedEpochGenerator().generate(),
                Email.restore("test%s@example.com".formatted((int) (Math.random() * Integer.MAX_VALUE))),
                EncodedPassword.from("encoded_password"),
                Role.PRODUCT_MANAGER,
                createdAt,
                createdAt.plusSeconds(30),
                null,
                false
            );
        }

        @Test
        @DisplayName("Should delete unverified accounts")
        void shouldDeleteUnverifiedAccounts ()
        {
            Instant threshold = Instant.now();

            Account account1 = getUnverifiedAccount(threshold.plusSeconds(10));
            Account account2 = getUnverifiedAccount(threshold);
            Account account3 = getUnverifiedAccount(threshold.minusSeconds(20));
            Account account4 = getUnverifiedAccount(threshold.minusSeconds(25));
            Account account5 = getUnverifiedAccount(threshold.minusSeconds(10));

            repository.save(account1);
            repository.save(account2);
            repository.save(account3);
            repository.save(account4);
            repository.save(account5);

            int deletedRows = repository.deleteUnverifiedAccounts(threshold, 2);

            assertEquals(2, deletedRows);
            assertTrue(repository.existsById(account1.getId()));
            assertTrue(repository.existsById(account2.getId()));
            assertFalse(repository.existsById(account3.getId()));
            assertFalse(repository.existsById(account4.getId()));
            assertTrue(repository.existsById(account5.getId()));
        }

        @Test
        @DisplayName("Should hold row locks until transaction commits")
        void shouldHoldRowLocksUntilTransactionCommits () throws Exception
        {
            Instant threshold = Instant.now();

            List<Account> accounts = List.of(
                getUnverifiedAccount(threshold.minusSeconds(10)),
                getUnverifiedAccount(threshold.minusSeconds(20))
            );

            saveAccountsInSeparateTransaction(accounts);

            assertLockAndDelete(
                accounts,
                threshold,
                2,
                repository::deleteUnverifiedAccounts
            );

            cleanDatabase();
        }

        @Test
        @DisplayName("Should skip locked rows and delete")
        void shouldSkipLockedRowsAndDelete () throws Exception
        {
            Instant threshold = Instant.now();

            List<Account> accounts = List.of(
                getUnverifiedAccount(threshold.minusSeconds(10)),
                getUnverifiedAccount(threshold.minusSeconds(20))
            );

            saveAccountsInSeparateTransaction(accounts);

            Consumer<Account> lockFunction = a -> repository.findByIdAndLock(a.getId()).orElseThrow();

            assertDeleteSkipLocked(
                accounts.get(0),
                lockFunction,
                repository::deleteUnverifiedAccounts,
                threshold,
                2
            );

            assertTrue(repository.existsById(accounts.get(0).getId()));
            assertFalse(repository.existsById(accounts.get(1).getId()));

            cleanDatabase();
        }
    }

    @Nested
    @DisplayName("Delete accounts scheduled for deletion tests")
    class DeleteAccountScheduledForDeletionTests
    {
        private Account getAccountScheduledForDeletion (Instant deletionScheduledAt)
        {
            return Account.restore(
                Generators.timeBasedEpochGenerator().generate(),
                Email.restore("test%s@example.com".formatted((int) (Math.random() * Integer.MAX_VALUE))),
                EncodedPassword.from("encoded_password"),
                Role.PRODUCT_MANAGER,
                deletionScheduledAt.minusSeconds(30),
                deletionScheduledAt,
                deletionScheduledAt,
                true
            );
        }

        @Test
        @DisplayName("Should delete account scheduled for deletion")
        void shouldDeleteAccountScheduledForDeletion ()
        {
            Instant threshold = Instant.now();

            Account account1 = getAccountScheduledForDeletion(threshold.plusSeconds(10));
            Account account2 = getAccountScheduledForDeletion(threshold);
            Account account3 = getAccountScheduledForDeletion(threshold.minusSeconds(20));
            Account account4 = getAccountScheduledForDeletion(threshold.minusSeconds(25));
            Account account5 = getAccountScheduledForDeletion(threshold.minusSeconds(10));

            repository.save(account1);
            repository.save(account2);
            repository.save(account3);
            repository.save(account4);
            repository.save(account5);

            int deletedRows = repository.deleteAccountsScheduledForDeletion(threshold, 2);

            assertEquals(2, deletedRows);
            assertTrue(repository.existsById(account1.getId()));
            assertTrue(repository.existsById(account2.getId()));
            assertFalse(repository.existsById(account3.getId()));
            assertFalse(repository.existsById(account4.getId()));
            assertTrue(repository.existsById(account5.getId()));
        }

        @Test
        @DisplayName("Should hold row locks until transaction commits")
        void shouldHoldRowLocksUntilTransactionCommits () throws Exception
        {
            Instant threshold = Instant.now();

            List<Account> accounts = List.of(
                getAccountScheduledForDeletion(threshold.minusSeconds(10)),
                getAccountScheduledForDeletion(threshold.minusSeconds(20))
            );

            saveAccountsInSeparateTransaction(accounts);

            assertLockAndDelete(
                accounts,
                threshold,
                2,
                repository::deleteAccountsScheduledForDeletion
            );

            cleanDatabase();
        }

        @Test
        @DisplayName("Should skip locked rows and delete")
        void shouldSkipLockedRowsAndDelete () throws Exception
        {
            Instant threshold = Instant.now();

            List<Account> accounts = List.of(
                getAccountScheduledForDeletion(threshold.minusSeconds(10)),
                getAccountScheduledForDeletion(threshold.minusSeconds(20))
            );

            saveAccountsInSeparateTransaction(accounts);

            Consumer<Account> lockFunction = a -> repository.findByIdAndLock(a.getId()).orElseThrow();

            assertDeleteSkipLocked(
                accounts.get(0),
                lockFunction,
                repository::deleteAccountsScheduledForDeletion,
                threshold,
                2
            );

            assertTrue(repository.existsById(accounts.get(0).getId()));
            assertFalse(repository.existsById(accounts.get(1).getId()));

            cleanDatabase();
        }
    }
}
