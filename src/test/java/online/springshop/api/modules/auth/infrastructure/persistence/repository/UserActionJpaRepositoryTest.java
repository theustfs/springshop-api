package online.springshop.api.modules.auth.infrastructure.persistence.repository;

import com.fasterxml.uuid.Generators;
import jakarta.persistence.EntityManager;
import online.springshop.api.modules.auth.domain.account.Role;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.ConfirmationChallengeEntity;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.UserActionEntity;
import online.springshop.api.testsupport.AbstractDataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserActionJpaRepositoryTest extends AbstractDataJpaTest
{
    @Autowired
    private UserActionJpaRepository userActionRepository;

    @Autowired
    private AccountJpaRepository accountRepository;

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

    private ConfirmationChallengeEntity createConfirmationChallenge ()
    {
        return new ConfirmationChallengeEntity(
            null,
            "encodedConfirmationCode",
            0,
            false,
            null,
            null,
            null
        );
    }

    private UserActionEntity createUserActionEntity ()
    {
        UserActionEntity userActionEntity = new UserActionEntity(
            Generators.timeBasedEpochGenerator().generate(),
            ACCOUNT_ID,
            ActionType.ACCOUNT_REGISTRATION
        );

        userActionEntity.addChallenge(createConfirmationChallenge());
        userActionEntity.addChallenge(createConfirmationChallenge());

        return userActionEntity;
    }

    @Nested
    @DisplayName("Save tests")
    class SaveTests
    {
        @Test
        @DisplayName("Should keep challenge order on save")
        void shouldKeepChallengeOrderOnSave ()
        {
            UserActionEntity userActionEntity = createUserActionEntity();
            userActionRepository.save(userActionEntity);

            UserActionEntity savedUserActionEntity = userActionRepository.findById(userActionEntity.getId()).orElseThrow();

            assertEquals(userActionEntity.getChallenges().size(), savedUserActionEntity.getChallenges().size());

            for (int i=0; i < userActionEntity.getChallenges().size(); i++)
            {
                ConfirmationChallengeEntity cce1 = userActionEntity.getChallenges().get(i);
                ConfirmationChallengeEntity cce2 = savedUserActionEntity.getChallenges().get(i);

                assertEquals(i, cce2.getChallengeOrder());
                assertEquals(cce1.getEncodedConfirmationCode(), cce2.getEncodedConfirmationCode());
                assertEquals(cce1.getAttempts(), cce2.getAttempts());
                assertEquals(cce1.isCompleted(), cce2.isCompleted());
                assertEquals(cce1.getExpiresAt(), cce2.getExpiresAt());
            }
        }
    }

    @Nested
    @DisplayName("Delete tests")
    class DeleteTests
    {
        private int countChallenges (UUID userActionId)
        {
            Number result = (Number) entityManager.createNativeQuery(
                "select count (*) from confirmation_challenges cc where cc.user_action_id = :id"
            ).setParameter("id", userActionId).getSingleResult();

            return result.intValue();
        }

        @Test
        @DisplayName("Should delete challenges after deleted")
        void shouldDeleteChallengeOnDelete ()
        {
            UserActionEntity userActionEntity = createUserActionEntity();

            userActionRepository.save(userActionEntity);
            userActionRepository.deleteById(userActionEntity.getId());

            assertFalse(userActionRepository.existsById(userActionEntity.getId()));
            assertEquals(0, countChallenges(userActionEntity.getId()));
        }

        @Test
        @DisplayName("Should be deleted after account is deleted")
        void shouldBeDeletedAfterAccountIsDeleted ()
        {
            UserActionEntity userActionEntity = createUserActionEntity();

            userActionRepository.save(userActionEntity);
            accountRepository.deleteById(ACCOUNT_ID);

            entityManager.flush();
            entityManager.clear();

            assertFalse(userActionRepository.existsById(userActionEntity.getId()));
            assertEquals(0, countChallenges(userActionEntity.getId()));
        }
    }
}