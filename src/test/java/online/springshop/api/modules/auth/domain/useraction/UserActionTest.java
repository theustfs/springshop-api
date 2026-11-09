package online.springshop.api.modules.auth.domain.useraction;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.auth.domain.useraction.abstraction.ConfirmationCodeMatcher;
import online.springshop.api.modules.auth.domain.useraction.exception.UserActionConfirmationError;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationChallengePolicy;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationCodePolicy;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.shared.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User action tests")
class UserActionTest
{
    private static final UUID DEFAULT_ACCOUNT_ID = Generators.timeBasedEpochGenerator().generate();
    private static final UUID DEFAULT_ID = Generators.timeBasedEpochGenerator().generate();
    private static final ActionType DEFAULT_ACTION_TYPE = ActionType.ACCOUNT_REGISTRATION;

    private ConfirmationChallenge getConfirmationChallenge (
        long id,
        int attempts,
        boolean completed,
        Instant expiresAt
    )
    {
        return ConfirmationChallenge.restore(
            id,
            EncodedConfirmationCode.from("encodedConfirmationCode"),
            attempts,
            completed,
            expiresAt
        );
    }

    private ConfirmationChallenge getConfirmationChallengeWithExpiration (
        Instant expiresAt
    )
    {
        long id = (long) (Math.random() * Long.MAX_VALUE);
        int attempts = 0;
        boolean completed = false;

        return getConfirmationChallenge(id, attempts, completed, expiresAt);
    }

    private UserAction getUserAction (
        List<ConfirmationChallenge> challenges
    )
    {
        return UserAction.restore(
            DEFAULT_ID,
            DEFAULT_ACCOUNT_ID,
            DEFAULT_ACTION_TYPE,
            challenges
        );
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(getConfirmationChallengeWithExpiration(null));

            assertThrows(
                NullPointerException.class,
                () -> UserAction.from(
                    accountId,
                    actionType,
                    challenges,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should fail when challenge list is empty")
        void shouldFailWhenChallengeListIsEmpty ()
        {
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of();
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

            assertThrows(
                IllegalArgumentException.class,
                () -> UserAction.from(
                    accountId,
                    actionType,
                    challenges,
                    clock
                )
            );
        }

        @Test
        @DisplayName("Should fail when challenge list already contains active challenges")
        void shouldFailWhenChallengeListAlreadyContainsActiveChallenges ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(Instant.now())
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> UserAction.from(
                    accountId,
                    actionType,
                    challenges,
                    clock
                )
            );
        }

        @Test
        @DisplayName("Should fail when account id is null")
        void shouldFailWhenAccountIdIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(null)
            );

            assertThrows(
                NullPointerException.class,
                () -> UserAction.from(
                    null,
                    actionType,
                    challenges,
                    clock
                )
            );
        }

        @Test
        @DisplayName("Should fail when action type is null")
        void shouldFailWhenActionTypeIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(null)
            );

            assertThrows(
                NullPointerException.class,
                () -> UserAction.from(
                    accountId,
                    null,
                    challenges,
                    clock
                )
            );
        }

        @Test
        @DisplayName("Should fail when challenge list contains null elements")
        void shouldFailWhenChallengeListContainsNullElement ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = new ArrayList<>();

            challenges.add(null);

            assertThrows(
                NullPointerException.class,
                () -> UserAction.from(
                    accountId,
                    actionType,
                    challenges,
                    clock
                )
            );
        }

        @Test
        @DisplayName("Should create user action")
        void shouldCreateUserAction ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_DELETION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(null)
            );

            UserAction userAction = UserAction.from(
                accountId,
                actionType,
                challenges,
                clock
            );

            assertNotNull(userAction);
            assertNotNull(userAction.getId());
            assertEquals(accountId, userAction.getAccountId());
            assertEquals(actionType, userAction.getActionType());
            assertEquals(challenges, userAction.getChallenges());
            assertTrue(userAction.getChallenges().get(0).isActive());
        }
    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should fail when challenge list is empty")
        void shouldFailWhenChallengeListIsEmpty ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of();

            assertThrows(
                IllegalArgumentException.class,
                () -> UserAction.restore(
                    id,
                    accountId,
                    actionType,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should fail when current challenge is unactive")
        void shouldFailWhenCurrentChallengesIsUnactive ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallenge(1L, 1, true, Instant.now()),
                getConfirmationChallenge(2L, 0, false, null)
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> UserAction.restore(
                    id,
                    accountId,
                    actionType,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should fail when id is null")
        void shouldFailWhenIdIsNull ()
        {
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(Instant.now())
            );

            assertThrows(
                NullPointerException.class,
                () -> UserAction.restore(
                    null,
                    accountId,
                    actionType,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should fail when account id is null")
        void shouldFailWhenAccountIdIsNull ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(Instant.now())
            );

            assertThrows(
                NullPointerException.class,
                () -> UserAction.restore(
                    id,
                    null,
                    actionType,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should fail when action type is null")
        void shouldFailWhenActionTypeIsNull ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(Instant.now())
            );

            assertThrows(
                NullPointerException.class,
                () -> UserAction.restore(
                    id,
                    accountId,
                    null,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should fail when challenge list contains null elements")
        void shouldFailWhenChallengeListContainsNullElement ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = new ArrayList<>();

            challenges.add(null);

            assertThrows(
                NullPointerException.class,
                () -> UserAction.restore(
                    id,
                    accountId,
                    actionType,
                    challenges
                )
            );
        }

        @Test
        @DisplayName("Should restore user action")
        void shouldRestoreUserAction ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();
            UUID accountId = Generators.timeBasedEpochGenerator().generate();
            ActionType actionType = ActionType.ACCOUNT_REGISTRATION;
            List<ConfirmationChallenge> challenges = List.of(
                getConfirmationChallengeWithExpiration(Instant.now())
            );

            UserAction userAction = UserAction.restore(
                id,
                accountId,
                actionType,
                challenges
            );

            assertNotNull(userAction);
            assertEquals(accountId, userAction.getAccountId());
            assertEquals(actionType, userAction.getActionType());
            assertEquals(challenges, userAction.getChallenges());
        }
    }

    @Nested
    @DisplayName("Challenge completion tests")
    class ChallengeCompletionTests
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
        @DisplayName("Should fail when confirmation code is null")
        void shouldFailWhenConfirmationCodeIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, false, clock.instant().plusSeconds(30))
                )
            );

            ConfirmationChallenge currentChallenge = userAction.getChallenges().get(0);
            int currentChallengeAttemptsBefore = currentChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> userAction.completeCurrentChallenge(
                    null,
                    TEST_CONFIRMATION_CODE_MATCHER,
                    clock
                )
            );

            assertFalse(currentChallenge.isCompleted());
            assertEquals(currentChallengeAttemptsBefore, currentChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should fail when confirmation code matcher is null")
        void shouldFailWhenConfirmationCodeMatcherIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, false, clock.instant().plusSeconds(30))
                )
            );

            ConfirmationChallenge currentChallenge = userAction.getChallenges().get(0);
            int currentChallengeAttemptsBefore = currentChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> userAction.completeCurrentChallenge(
                    confirmationCode,
                    null,
                    clock
                )
            );

            assertFalse(currentChallenge.isCompleted());
            assertEquals(currentChallengeAttemptsBefore, currentChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, false, clock.instant().plusSeconds(30))
                )
            );

            ConfirmationChallenge currentChallenge = userAction.getChallenges().get(0);
            int currentChallengeAttemptsBefore = currentChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> userAction.completeCurrentChallenge(
                    confirmationCode,
                    TEST_CONFIRMATION_CODE_MATCHER,
                    null
                )
            );

            assertFalse(currentChallenge.isCompleted());
            assertEquals(currentChallengeAttemptsBefore, currentChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should return success result when all challenges are already completed")
        void shouldReturnSuccessResultWhenAllChallengesAreAlreadyCompleted ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(INCORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, true, clock.instant().plusSeconds(30))
                )
            );

            Result<Void, UserActionConfirmationError> result = userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should fail when confirmation code is incorrect")
        void shouldFailWhenConfirmationCodeIsIncorrect ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(INCORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, false, clock.instant().plusSeconds(30))
                )
            );

            Result<Void, UserActionConfirmationError> result = userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertEquals(UserActionConfirmationError.CONFIRMATION_CODE_DOES_NOT_MATCH, result.error().code());
        }

        @Test
        @DisplayName("Should fail when challenge reaches maximum number of attempts")
        void shouldFailWhenChallengeReachesMaximumNumberOfAttempts ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, ConfirmationChallengePolicy.MAX_ATTEMPTS, false, clock.instant().plusSeconds(30))
                )
            );

            Result<Void, UserActionConfirmationError> result = userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertEquals(UserActionConfirmationError.CONFIRMATION_CHALLENGE_REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS, result.error().code());
            assertFalse(userAction.getChallenges().get(0).isCompleted());
        }

        @Test
        @DisplayName("Should fail when current challenge is expired")
        void shouldFailWhenCurrentChallengeIsExpired ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, false, clock.instant().minusSeconds(30))
                )
            );

            Result<Void, UserActionConfirmationError> result = userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertEquals(UserActionConfirmationError.CONFIRMATION_CHALLENGE_EXPIRED, result.error().code());
        }

        @Test
        @DisplayName("Should complete current challenge when confirmation code is correct")
        void shouldCompleteCurrentChallengeWhenConfirmationCodeIsCorrect ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 0, false, clock.instant().plusSeconds(30))
                )
            );

            Result<Void, UserActionConfirmationError> result = userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("Should activate next challenge after completing current challenge")
        void shouldActivateNextChallengeAfterCompletingCurrentChallenge ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            UserAction userAction = UserAction.from(
                DEFAULT_ACCOUNT_ID,
                DEFAULT_ACTION_TYPE,
                List.of(
                    getConfirmationChallenge(1L, 0, false, null),
                    getConfirmationChallenge(2L, 0, false, null)
                ),
                clock
            );

            userAction.completeCurrentChallenge(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(userAction.getChallenges().get(1).isActive());
        }
    }

    @Nested
    @DisplayName("Challenges completion state report tests")
    class ChallengesCompletionStateReportTests
    {
        @Test
        @DisplayName("Should report false when not all challenges were completed")
        void shouldReportFalseWhenNotAllChallengesAreCompleted ()
        {
            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, true, Instant.now().plusSeconds(30)),
                    getConfirmationChallenge(2L, 0, false, Instant.now().plusSeconds(30))
                )
            );

            assertFalse(userAction.areAllChallengesCompleted());
        }

        @Test
        @DisplayName("Should report true when all challenges were completed")
        void shouldReportTrueWhenAllChallengesAreCompleted ()
        {
            UserAction userAction = getUserAction(
                List.of(
                    getConfirmationChallenge(1L, 1, true, Instant.now()),
                    getConfirmationChallenge(2L, 2, true, Instant.now().plusSeconds(30))
                )
            );

            assertTrue(userAction.areAllChallengesCompleted());
        }
    }

    @Nested
    @DisplayName("Equals and hash code tests")
    class EqualsAndHashCodeTests
    {
        private UserAction getUserActionWithId (
            UUID id
        )
        {
            return UserAction.restore(
                id,
                Generators.timeBasedEpochGenerator().generate(),
                DEFAULT_ACTION_TYPE,
                List.of(getConfirmationChallengeWithExpiration(Instant.now()))
            );
        }

        @Test
        @DisplayName("Should be equal when IDs are equal")
        void shouldBeEqualWhenIdsAreEqual ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();

            UserAction userAction1 = getUserActionWithId(id);
            UserAction userAction2 = getUserActionWithId(id);

            assertEquals(userAction1, userAction2);
        }

        @Test
        @DisplayName("Should not be equal when IDs are not equal")
        void shouldNotBeEqualWhenIdsAreNotEqual ()
        {
            UUID id1 = Generators.timeBasedEpochGenerator().generate();
            UUID id2 = Generators.timeBasedEpochGenerator().generate();

            UserAction userAction1 = getUserActionWithId(id1);
            UserAction userAction2 = getUserActionWithId(id2);

            assertNotEquals(userAction1, userAction2);
        }

        @Test
        @DisplayName("Hash code should be equal when IDs are equal")
        void hashCodeShouldBeEqualWhenIdsAreEqual ()
        {
            UUID id = Generators.timeBasedEpochGenerator().generate();

            UserAction userAction1 = getUserActionWithId(id);
            UserAction userAction2 = getUserActionWithId(id);

            assertEquals(userAction1.hashCode(), userAction2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when IDs are not equal")
        void hashCodeShouldNotBeEqualIdsAreNotEqual ()
        {
            UUID id1 = Generators.timeBasedEpochGenerator().generate();
            UUID id2 = Generators.timeBasedEpochGenerator().generate();

            UserAction userAction1 = getUserActionWithId(id1);
            UserAction userAction2 = getUserActionWithId(id2);

            assertNotEquals(userAction1.hashCode(), userAction2.hashCode());
        }
    }
}