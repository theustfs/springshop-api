package online.springshop.api.modules.auth.domain.useraction;

import online.springshop.api.modules.auth.domain.useraction.abstraction.ConfirmationCodeMatcher;
import online.springshop.api.modules.auth.domain.useraction.exception.ConfirmationChallengeCompletionError;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationChallengePolicy;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationCodePolicy;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.shared.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Confirmation challenge tests")
class ConfirmationChallengeTest
{
    private EncodedConfirmationCode getEncodedConfirmationCode ()
    {
        return EncodedConfirmationCode.from("encodedConfirmationCode");
    }

    private ConfirmationChallenge getConfirmationChallenge (
        Long id,
        EncodedConfirmationCode encodedConfirmationCode,
        int attempts,
        boolean completed,
        Instant expiresAt
    )
    {
        return ConfirmationChallenge.restore(
            id,
            encodedConfirmationCode,
            attempts,
            completed,
            expiresAt
        );
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should create confirmation challenge")
        void shouldCreateConfirmationChallenge ()
        {
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            ConfirmationChallenge confirmationChallenge = ConfirmationChallenge.from(encodedConfirmationCode);

            assertNotNull(confirmationChallenge);
            assertNull(confirmationChallenge.getId());
            assertEquals(encodedConfirmationCode, confirmationChallenge.getEncodedConfirmationCode());
            assertEquals(0, confirmationChallenge.getAttempts());
            assertFalse(confirmationChallenge.isCompleted());
            assertNull(confirmationChallenge.getExpiresAt());
        }

        @Test
        @DisplayName("Should fail when encoded confirmation code is null")
        void shouldFailWhenEncodedConfirmationCodeIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> ConfirmationChallenge.from(null)
            );
        }
    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should fail when id is null")
        void shouldFailWhenIdIsNull ()
        {
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            int attempts = 2;
            boolean completed = false;
            Instant expiresAt = Instant.now();

            assertThrows(
                NullPointerException.class,
                () -> ConfirmationChallenge.restore(
                    null,
                    encodedConfirmationCode,
                    attempts,
                    completed,
                    expiresAt
                )
            );
        }

        @Test
        @DisplayName("Should fail when attempt count is negative")
        void shouldFailWhenAttemptCountIsNegative ()
        {
            Long id = 16L;
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            int attempts = -1;
            boolean completed = false;
            Instant expiresAt = Instant.now();

            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationChallenge.restore(
                    id,
                    encodedConfirmationCode,
                    attempts,
                    completed,
                    expiresAt
                )
            );
        }

        @Test
        @DisplayName("Should fail when attempt count exceeds the limit")
        void shouldFailWhenAttemptCountExceedsTheLimit ()
        {
            Long id = 16L;
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            int attempts = ConfirmationChallengePolicy.MAX_ATTEMPTS + 1;
            boolean completed = false;
            Instant expiresAt = Instant.now();

            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationChallenge.restore(
                    id,
                    encodedConfirmationCode,
                    attempts,
                    completed,
                    expiresAt
                )
            );
        }

        @Test
        @DisplayName("Should fail when completed without attempts")
        void shouldFailWhenCompletedWithoutAttempts ()
        {
            Long id = 16L;
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            int attempts = 0;
            boolean completed = true;
            Instant expiresAt = Instant.now();

            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationChallenge.restore(
                    id,
                    encodedConfirmationCode,
                    attempts,
                    completed,
                    expiresAt
                )
            );
        }

        @Test
        @DisplayName("Should restore confirmation challenge")
        void shouldRestoreConfirmationChallenge ()
        {
            Long id = 16L;
            EncodedConfirmationCode encodedConfirmationCode = getEncodedConfirmationCode();
            int attempts = 2;
            boolean completed = true;
            Instant expiresAt = Instant.now();

            ConfirmationChallenge confirmationChallenge = ConfirmationChallenge.restore(
                id,
                encodedConfirmationCode,
                attempts,
                completed,
                expiresAt
            );

            assertNotNull(confirmationChallenge);
            assertEquals(id, confirmationChallenge.getId());
            assertEquals(encodedConfirmationCode, confirmationChallenge.getEncodedConfirmationCode());
            assertEquals(attempts, confirmationChallenge.getAttempts());
            assertEquals(completed, confirmationChallenge.isCompleted());
            assertEquals(expiresAt, confirmationChallenge.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("Activation tests")
    class ActivationTests
    {
        @Test
        @DisplayName("Should activate")
        void shouldActivate ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                null
            );

            confirmationChallenge.activate(clock);

            assertEquals(clock.instant().plus(ConfirmationChallengePolicy.DURATION), confirmationChallenge.getExpiresAt());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                null
            );

            assertThrows(
                NullPointerException.class,
                () -> confirmationChallenge.activate(null)
            );
        }

        @Test
        @DisplayName("Should do nothing when already active")
        void shouldDoNothingWhenAlreadyActive ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                clock.instant().minusSeconds(30)
            );

            Instant expiresAtBefore = confirmationChallenge.getExpiresAt();

            confirmationChallenge.activate(clock);

            assertEquals(expiresAtBefore, confirmationChallenge.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("Completion tests")
    class CompletionTests
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
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                1,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> confirmationChallenge.complete(
                    confirmationCode,
                    TEST_CONFIRMATION_CODE_MATCHER,
                    null
                )
            );

            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should fail when confirmation code is null")
        void shouldFailWhenConfirmationCodeIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                1,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> confirmationChallenge.complete(
                    null,
                    TEST_CONFIRMATION_CODE_MATCHER,
                    clock
                )
            );

            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should fail when confirmation code matcher is null")
        void shouldFailWhenConfirmationCodeMatcherIsNull ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                1,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            assertThrows(
                NullPointerException.class,
                () -> confirmationChallenge.complete(
                    confirmationCode,
                    null,
                    clock
                )
            );

            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should do nothing when already completed")
        void shouldDoNothingWhenAlreadyCompleted ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                1,
                true,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            Result<Void, ConfirmationChallengeCompletionError> result = confirmationChallenge.complete(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isSuccess());
            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
        }

        @Test
        @DisplayName("Should fail when challenge has expired")
        void shouldFailWhenChallengeHasExpired ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                clock.instant().minusSeconds(1)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            Result<Void, ConfirmationChallengeCompletionError> result = confirmationChallenge.complete(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
            assertFalse(confirmationChallenge.isCompleted());
            assertEquals(ConfirmationChallengeCompletionError.EXPIRED, result.error().code());
        }

        @Test
        @DisplayName("Should fail when maximum number of attempts is reached")
        void shouldFailWhenMaximumNumberOfAttemptsIsReached ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                ConfirmationChallengePolicy.MAX_ATTEMPTS,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            Result<Void, ConfirmationChallengeCompletionError> result = confirmationChallenge.complete(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertEquals(attemptsBefore, confirmationChallenge.getAttempts());
            assertFalse(confirmationChallenge.isCompleted());
            assertEquals(ConfirmationChallengeCompletionError.REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS, result.error().code());
        }

        @Test
        @DisplayName("Should fail when code does not match")
        void shouldFailWhenCodeNotMatch ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(INCORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            Result<Void, ConfirmationChallengeCompletionError> result = confirmationChallenge.complete(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isFailure());
            assertFalse(confirmationChallenge.isCompleted());
            assertEquals(attemptsBefore + 1, confirmationChallenge.getAttempts());
            assertEquals(ConfirmationChallengeCompletionError.CONFIRMATION_CODE_DOES_NOT_MATCH, result.error().code());
        }

        @Test
        @DisplayName("Should complete when code matches")
        void shouldCompleteWhenCodeMatches ()
        {
            Clock clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
            ConfirmationCode confirmationCode = ConfirmationCode.from(CORRECT_CONFIRMATION_CODE);

            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                clock.instant().plusSeconds(30)
            );

            int attemptsBefore = confirmationChallenge.getAttempts();

            Result<Void, ConfirmationChallengeCompletionError> result = confirmationChallenge.complete(
                confirmationCode,
                TEST_CONFIRMATION_CODE_MATCHER,
                clock
            );

            assertTrue(result.isSuccess());
            assertTrue(confirmationChallenge.isCompleted());
            assertEquals(attemptsBefore + 1, confirmationChallenge.getAttempts());
        }
    }

    @Nested
    @DisplayName("Active status report tests")
    class ActiveStatusReportTests
    {
        @Test
        @DisplayName("Should report true when active")
        void shouldReportTrueWhenActive ()
        {
            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                Instant.now()
            );

            assertTrue(confirmationChallenge.isActive());
        }

        @Test
        @DisplayName("Should report false when not active")
        void shouldReportFalseWhenNotActive ()
        {
            ConfirmationChallenge confirmationChallenge = getConfirmationChallenge(
                16L,
                getEncodedConfirmationCode(),
                0,
                false,
                null
            );

            assertFalse(confirmationChallenge.isActive());
        }
    }

    @Nested
    @DisplayName("Equals and hash code tests")
    class EqualsAndHashCodeTests
    {
        private ConfirmationChallenge getConfirmationChallengeWithId (Long id)
        {
            return getConfirmationChallenge(
                id,
                getEncodedConfirmationCode(),
                0,
                false,
                Instant.now()
            );
        }

        @Test
        @DisplayName("Should be equal when ID is not null and IDs are equal")
        void shouldBeEqualWhenIDIsNotNullAndIDsAreEqual ()
        {
            Long id1 = 1L;

            ConfirmationChallenge confirmationChallenge1 = getConfirmationChallengeWithId(id1);
            ConfirmationChallenge confirmationChallenge2 = getConfirmationChallengeWithId(id1);

            assertEquals(confirmationChallenge1, confirmationChallenge2);
        }

        @Test
        @DisplayName("Should not be equal when ID is null")
        void shouldNotBeEqualWhenIDIsNull ()
        {
            Long id1 = 1L;

            ConfirmationChallenge confirmationChallenge1 = ConfirmationChallenge.from(getEncodedConfirmationCode());
            ConfirmationChallenge confirmationChallenge2 = getConfirmationChallengeWithId(id1);

            assertNotEquals(confirmationChallenge1, confirmationChallenge2);
        }

        @Test
        @DisplayName("Should not be equal when IDs are not equal")
        void shouldNotBeEqualWhenIDsAreNotEqual ()
        {
            Long id1 = 1L;
            Long id2 = 2L;

            ConfirmationChallenge confirmationChallenge1 = getConfirmationChallengeWithId(id1);
            ConfirmationChallenge confirmationChallenge2 = getConfirmationChallengeWithId(id2);

            assertNotEquals(confirmationChallenge1, confirmationChallenge2);
        }

        @Test
        @DisplayName("Hash code should be equal when ID is not null and IDs are equal")
        void hashCodeShouldBeEqualWhenIDIsNotNullAndIDsAreEqual ()
        {
            Long id1 = 1L;

            ConfirmationChallenge confirmationChallenge1 = getConfirmationChallengeWithId(id1);
            ConfirmationChallenge confirmationChallenge2 = getConfirmationChallengeWithId(id1);

            assertEquals(confirmationChallenge1.hashCode(), confirmationChallenge2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when IDs are not equal")
        void hashCodeShouldNotBeEqualWhenIdsAreNotEqual ()
        {
            Long id1 = 1L;
            Long id2 = 2L;

            ConfirmationChallenge confirmationChallenge1 = getConfirmationChallengeWithId(id1);
            ConfirmationChallenge confirmationChallenge2 = getConfirmationChallengeWithId(id2);

            assertNotEquals(confirmationChallenge1.hashCode(), confirmationChallenge2.hashCode());
        }
    }
}