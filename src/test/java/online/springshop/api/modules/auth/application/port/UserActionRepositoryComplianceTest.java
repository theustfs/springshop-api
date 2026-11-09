package online.springshop.api.modules.auth.application.port;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;

import online.springshop.api.modules.auth.domain.account.Role;
import online.springshop.api.modules.auth.domain.useraction.ConfirmationChallenge;
import online.springshop.api.modules.auth.domain.useraction.UserAction;
import online.springshop.api.modules.auth.domain.useraction.abstraction.ConfirmationCodeMatcher;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationCodePolicy;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.UserActionEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.mapper.AccountMapperImpl;
import online.springshop.api.modules.auth.infrastructure.persistence.mapper.UserActionMapperImpl;
import online.springshop.api.modules.auth.infrastructure.persistence.repository.AccountJpaRepository;
import online.springshop.api.modules.auth.infrastructure.persistence.repository.AccountRepositoryJpaAdapter;
import online.springshop.api.modules.auth.infrastructure.persistence.repository.UserActionJpaRepositoryAdapter;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    UserActionJpaRepositoryAdapter.class,
    AccountRepositoryJpaAdapter.class,
    UserActionMapperImpl.class,
    AccountMapperImpl.class
})
@DisplayName("User action repository compliance tests")
class UserActionRepositoryComplianceTest extends AbstractDataJpaTest
{


    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private AccountJpaRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private static final UUID ACCOUNT_ID = Generators.timeBasedEpochGenerator().generate();

    @BeforeEach
    public void createAccount ()
    {
        AccountEntity account = new AccountEntity(
            ACCOUNT_ID,
            "test%s@example.com".formatted((int) (Math.random() * Integer.MAX_VALUE)),
            "encodedPassword",
            Role.USER,
            Instant.now(),
            null,
            null,
            false
        );

        accountRepository.save(account);
    }

    private ConfirmationChallenge createConfirmationChallenge ()
    {
        return ConfirmationChallenge.from(
            EncodedConfirmationCode.from("encodedConfirmationCode%s".formatted((int) (Math.random() * Integer.MAX_VALUE)))
        );
    }

    private UserAction createUserAction (
        List<ConfirmationChallenge> challenges
    )
    {
        return UserAction.from(
            ACCOUNT_ID,
            ActionType.ACCOUNT_REGISTRATION,
            challenges,
            Clock.fixed(Instant.now(), ZoneOffset.UTC)
        );
    }

    private UserAction createSavedUserAction ()
    {
        UserAction userAction = createUserAction(
            List.of(
                createConfirmationChallenge(),
                createConfirmationChallenge()
            )
        );

        userActionRepository.save(userAction);

        return userAction;
    }

    private void cleanDatabase ()
    {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        transaction.executeWithoutResult(status -> {
            entityManager.createNativeQuery("delete from users_actions")
                .executeUpdate();

            entityManager.createNativeQuery("delete from accounts a where a.id = :accountId")
                .setParameter("accountId", ACCOUNT_ID)
                .executeUpdate();

            entityManager.clear();
        });
    }

    private void assertEqualUserActions (
        UserAction createdChallenge,
        UserAction savedChallenge
    )
    {
        assertEquals(createdChallenge.getId(), savedChallenge.getId());
        assertEquals(createdChallenge.getAccountId(), savedChallenge.getAccountId());
        assertEquals(createdChallenge.getActionType(), savedChallenge.getActionType());
        assertEquals(createdChallenge.getChallenges().size(), savedChallenge.getChallenges().size());

        // asserts challenge order is preserved
        for (int i = 0; i < createdChallenge.getChallenges().size(); i++)
        {
            ConfirmationChallenge cc1 = createdChallenge.getChallenges().get(i);
            ConfirmationChallenge cc2 = savedChallenge.getChallenges().get(i);

            assertNotNull(cc2.getId());
            assertEquals(cc1.getEncodedConfirmationCode(), cc2.getEncodedConfirmationCode());
            assertEquals(cc1.getAttempts(), cc2.getAttempts());
            assertEquals(cc1.isCompleted(), cc2.isCompleted());
            assertEquals(cc1.getExpiresAt(), cc2.getExpiresAt());
        }
    }

    private void saveUserActionInSeparateTransaction (UserAction userAction)
    {
        TransactionTemplate accountTransaction = new TransactionTemplate(transactionManager);
        accountTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        accountTransaction.executeWithoutResult(status -> {
            createAccount();

            userActionRepository.save(userAction);
        });
    }

    private UserActionEntity acquireLock (UUID id)
    {
        TransactionTemplate accountTransaction = new TransactionTemplate(transactionManager);
        accountTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return accountTransaction.execute(status -> entityManager.find(
            UserActionEntity.class,
            id,
            LockModeType.PESSIMISTIC_WRITE,
            Map.of("jakarta.persistence.lock.timeout", 0)
        ));
    }

    private void assertSearchAndLock (
        UserAction userAction,
        Consumer<UserAction> lockFunction
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
                lockFunction.accept(userAction);

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
                () -> acquireLock(userAction.getId())
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

        assertTrue(userActionRepository.findById(userAction.getId()).isPresent());

        // assert row is not locked after transaction ends
        UserActionEntity entity = assertDoesNotThrow(() -> acquireLock(userAction.getId()));
        assertNotNull(entity);
    }

    @Nested
    @DisplayName("Save tests")
    class SaveTests
    {
        private static final String CORRECT_CONFIRMATION_CODE =
            "9".repeat(ConfirmationCodePolicy.LENGTH);

        private static final String INCORRECT_CONFIRMATION_CODE =
            "0".repeat(ConfirmationCodePolicy.LENGTH);

        private static class TestConfirmationCodeMatcher implements ConfirmationCodeMatcher
        {
            @Override
            public boolean matches (ConfirmationCode raw, EncodedConfirmationCode encoded)
            {
                return raw.getValue()
                    .equals(CORRECT_CONFIRMATION_CODE);
            }
        }

        private static final TestConfirmationCodeMatcher TEST_CONFIRMATION_CODE_MATCHER = new TestConfirmationCodeMatcher();

        @Test
        @DisplayName("Should save user action")
        void shouldSaveUserAction ()
        {
            UserAction userAction = createUserAction(
                List.of(
                    createConfirmationChallenge(),
                    createConfirmationChallenge()
                )
            );

            userActionRepository.save(userAction);
            UserAction savedUserAction = userActionRepository.findById(userAction.getId()).orElseThrow();

            assertEqualUserActions(userAction, savedUserAction);
        }

        @Test
        @DisplayName("Should update user action")
        void shouldUpdateUserAction ()
        {
            UserAction userAction = createUserAction(
                List.of(
                    createConfirmationChallenge(),
                    createConfirmationChallenge()
                )
            );

            userActionRepository.save(userAction);
            UserAction savedUserAction = userActionRepository.findById(userAction.getId()).orElseThrow();

            savedUserAction.completeCurrentChallenge(
                ConfirmationCode.from(INCORRECT_CONFIRMATION_CODE),
                TEST_CONFIRMATION_CODE_MATCHER,
                Clock.fixed(Instant.now(), ZoneOffset.UTC)
            );

            userActionRepository.save(savedUserAction);
            UserAction updatedUserAction = userActionRepository.findById(userAction.getId()).orElseThrow();

            assertEquals(userAction.getId(), updatedUserAction.getId());
            assertEquals(userAction.getAccountId(), updatedUserAction.getAccountId());
            assertEquals(userAction.getActionType(), updatedUserAction.getActionType());
            assertEquals(userAction.getChallenges().size(), updatedUserAction.getChallenges().size());
            assertEquals(
                userAction.getChallenges().get(0).getAttempts() + 1,
                updatedUserAction.getChallenges().get(0).getAttempts()
            );

            for (int i = 0; i < userAction.getChallenges().size(); i++)
            {
                ConfirmationChallenge cc1 = userAction.getChallenges().get(i);
                ConfirmationChallenge cc2 = updatedUserAction.getChallenges().get(i);

                assertNotNull(cc2.getId());
                assertEquals(cc1.getEncodedConfirmationCode(), cc2.getEncodedConfirmationCode());
                assertEquals(cc1.isCompleted(), cc2.isCompleted());
                assertEquals(cc1.getExpiresAt(), cc2.getExpiresAt());
            }
        }
    }

    @Nested
    @DisplayName("Find by id tests")
    class FindByIdTests
    {
        @Test
        @DisplayName("Should find by id")
        void shouldFindById ()
        {
            UserAction userAction = createSavedUserAction();

            UserAction savedUserAction = userActionRepository.findById(userAction.getId()).orElseThrow();

            assertEqualUserActions(userAction, savedUserAction);
        }

        @Test
        @DisplayName("Should return empty when it does not exist")
        void shouldReturnEmptyWhenItDoesNotExist ()
        {
            Optional<UserAction> userAction = userActionRepository.findById(Generators.timeBasedEpochGenerator().generate());

            assertTrue(userAction.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by id and lock tests")
    class FindByIdAndLockTests
    {
        @Test
        @DisplayName("Should find by id and lock")
        void shouldFindByIdAndLock () throws Exception
        {
            UserAction userAction = createUserAction(
                List.of(
                    createConfirmationChallenge(),
                    createConfirmationChallenge()
                )
            );
            saveUserActionInSeparateTransaction(userAction);

            Consumer<UserAction> function = a -> userActionRepository.findByIdAndLock(a.getId()).orElseThrow();

            assertSearchAndLock(userAction, function);
            cleanDatabase();
        }

        @Test
        @DisplayName("Should return empty when it does not exist")
        void shouldReturnEmptyWhenItDoesNotExist ()
        {
            Optional<UserAction> userAction = userActionRepository.findByIdAndLock(Generators.timeBasedEpochGenerator().generate());

            assertTrue(userAction.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by account id tests")
    class FindByAccountIdTests
    {
        @Test
        @DisplayName("Should find by id")
        void shouldFindById ()
        {
            UserAction userAction = createSavedUserAction();

            UserAction savedUserAction = userActionRepository.findByAccountId(ACCOUNT_ID).orElseThrow();

            assertEqualUserActions(userAction, savedUserAction);
            cleanDatabase();
        }

        @Test
        @DisplayName("Should return empty when it does not exist")
        void shouldReturnEmptyWhenItDoesNotExist ()
        {
            Optional<UserAction> userAction = userActionRepository.findByAccountId(Generators.timeBasedEpochGenerator().generate());

            assertTrue(userAction.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by account id and lock tests")
    class FindByAccountIdAndLockTests
    {
        @Test
        @DisplayName("Should find by id and lock")
        void shouldFindByAccountIdAndLock () throws Exception
        {
            UserAction userAction = createUserAction(
                List.of(
                    createConfirmationChallenge(),
                    createConfirmationChallenge()
                )
            );
            saveUserActionInSeparateTransaction(userAction);

            Consumer<UserAction> function = a -> userActionRepository.findByAccountIdAndLock(a.getAccountId()).orElseThrow();

            assertSearchAndLock(userAction, function);
            cleanDatabase();
        }

        @Test
        @DisplayName("Should return empty when it does not exist")
        void shouldReturnEmptyWhenItDoesNotExist ()
        {
            Optional<UserAction> userAction = userActionRepository.findByAccountIdAndLock(Generators.timeBasedEpochGenerator().generate());

            assertTrue(userAction.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete tests")
    class DeleteByIdTests
    {
        @Test
        @DisplayName("Should delete by id")
        void shouldDeleteById ()
        {
            UserAction userAction = createSavedUserAction();

            userActionRepository.deleteById(userAction.getId());

            assertTrue(userActionRepository.findById(userAction.getId()).isEmpty());
        }

        @Test
        @DisplayName("Should delete by account id")
        void shouldDeleteByAccountId ()
        {
            UserAction userAction = createSavedUserAction();

            userActionRepository.deleteByAccountId(ACCOUNT_ID);

            assertTrue(userActionRepository.findById(userAction.getId()).isEmpty());
        }
    }
}
