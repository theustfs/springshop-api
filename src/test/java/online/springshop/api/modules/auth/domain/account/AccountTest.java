package online.springshop.api.modules.auth.domain.account;

import online.springshop.api.fixtures.AccountFixtures;
import online.springshop.api.modules.auth.domain.account.exception.AccountUpdateFailedException;
import online.springshop.api.modules.auth.domain.account.exception.AccountVerificationFailedException;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Account tests")
class AccountTest
{
    private static final String FIXTURES = "online.springshop.api.fixtures.AccountFixtures";

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-01-01T10:05:00Z");
    private static final Instant UPDATE_TIME = Instant.parse("2026-01-01T10:10:00Z");

    private static final UUID ACCOUNT_ID = UUID.fromString("019b76da-a000-7000-8000-000000000001");
    private static final Email EMAIL = Email.restore("test@example.com");
    private static final EncodedPassword ENCODED_PASSWORD = EncodedPassword.from("encoded_password");

    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should create an unverified account with the default role")
        void shouldCreateAccount ()
        {
            Clock clock = Clock.fixed(CREATED_AT, ZoneOffset.UTC);

            Account account = Account.of(EMAIL, ENCODED_PASSWORD, clock);

            assertNotNull(account.getId());
            assertEquals(EMAIL, account.getEmail());
            assertEquals(ENCODED_PASSWORD, account.getEncodedPassword());
            assertEquals(Role.USER, account.getRole());
            assertEquals(CREATED_AT, account.getCreatedAt());
            assertNull(account.getUpdatedAt());
            assertNull(account.getDeletionScheduledAt());
            assertFalse(account.isVerified());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> Account.of(EMAIL, ENCODED_PASSWORD, null)
            );
        }

        @Test
        @DisplayName("Should fail when email is null")
        void shouldFailWhenEmailIsNull ()
        {
            Clock clock = Clock.fixed(CREATED_AT, ZoneOffset.UTC);

            assertThrows(
                NullPointerException.class,
                () -> Account.of(null, ENCODED_PASSWORD, clock)
            );
        }

        @Test
        @DisplayName("Should fail when encoded password is null")
        void shouldFailWhenEncodedPasswordIsNull ()
        {
            Clock clock = Clock.fixed(CREATED_AT, ZoneOffset.UTC);

            assertThrows(
                NullPointerException.class,
                () -> Account.of(EMAIL, null, clock)
            );
        }

    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should restore account")
        void shouldRestoreAccount ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertEquals(ACCOUNT_ID, account.getId());
            assertEquals(EMAIL, account.getEmail());
            assertEquals(ENCODED_PASSWORD, account.getEncodedPassword());
            assertEquals(Role.USER, account.getRole());
            assertEquals(CREATED_AT, account.getCreatedAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertNull(account.getDeletionScheduledAt());
            assertTrue(account.isVerified());
        }

        @Test
        @DisplayName("Should restore equal timestamps")
        void shouldRestoreEqualTimestamps ()
        {
            Account account = Account.restore(
                ACCOUNT_ID,
                EMAIL,
                ENCODED_PASSWORD,
                Role.USER,
                CREATED_AT,
                CREATED_AT,
                CREATED_AT,
                true
            );

            assertEquals(CREATED_AT, account.getUpdatedAt());
            assertEquals(CREATED_AT, account.getDeletionScheduledAt());
        }

        @Test
        @DisplayName("Should fail when deletion schedule is before creation")
        void shouldFailWhenDeletionScheduleIsBeforeCreation ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> restore(ACCOUNT_ID, CREATED_AT.minusSeconds(1), CREATED_AT.minusSeconds(1), true)
            );
        }

        @Test
        @DisplayName("Should fail when deletion schedule has no update time")
        void shouldFailWhenDeletionScheduleHasNoUpdateTime ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> restore(ACCOUNT_ID, null, UPDATED_AT, true)
            );
        }

        @Test
        @DisplayName("Should fail when deletion schedule differs from updated at")
        void shouldFailWhenDeletionScheduleDiffersFromUpdatedAt ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> restore(ACCOUNT_ID, UPDATED_AT, UPDATE_TIME, true)
            );
        }

        @Test
        @DisplayName("Should fail when update time is before creation")
        void shouldFailWhenUpdateTimeIsBeforeCreation ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> restore(ACCOUNT_ID, CREATED_AT.minusSeconds(1), null, false)
            );
        }

        @Test
        @DisplayName("Should fail when verified account has no update time")
        void shouldFailWhenVerifiedAccountHasNoUpdateTime ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> restore(ACCOUNT_ID, null, null, true)
            );
        }

        @Test
        @DisplayName("Should fail when required state is null")
        void shouldFailWhenRequiredStateIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> Account.restore(null, EMAIL, ENCODED_PASSWORD, Role.USER, CREATED_AT, null, null, false)
            );
            assertThrows(
                NullPointerException.class,
                () -> Account.restore(ACCOUNT_ID,null, ENCODED_PASSWORD, Role.USER, CREATED_AT, null, null, false)
            );
            assertThrows(
                NullPointerException.class,
                () -> Account.restore(ACCOUNT_ID, EMAIL, null, Role.USER, CREATED_AT, null, null, false)
            );
            assertThrows(
                NullPointerException.class,
                () -> Account.restore(ACCOUNT_ID, EMAIL, ENCODED_PASSWORD, null, CREATED_AT, null, null, false)
            );
            assertThrows(
                NullPointerException.class,
                () -> Account.restore(ACCOUNT_ID, EMAIL, ENCODED_PASSWORD, Role.USER, null, null, null, false)
            );
        }

    }

    @Nested
    @DisplayName("Verification tests")
    class VerificationTests
    {
        @Test
        @DisplayName("Should verify account")
        void shouldVerifyAccount ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, false);

            account.verify(clock);

            assertTrue(account.isVerified());
            assertEquals(UPDATE_TIME, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should do nothing when already verified")
        void shouldDoNothingWhenAlreadyVerified ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            account.verify(clock);

            assertTrue(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, false);

            assertThrows(NullPointerException.class, () -> account.verify(null));

            assertFalse(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when scheduled for deletion")
        void shouldFailWhenScheduledForDeletion ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(USER_ID, UPDATED_AT, UPDATED_AT, false);

            assertThrows(UserVerificationFailedException.class, () -> account.verify(clock));

            assertFalse(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when verification time is before creation")
        void shouldFailWhenVerificationTimeIsBeforeCreation ()
        {
            Clock clock = Clock.fixed(CREATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, false);

            assertThrows(IllegalArgumentException.class, () -> account.verify(clock));

            assertFalse(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when verification time is before update time")
        void shouldFailWhenVerificationTimeIsBeforeUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, false);

            assertThrows(IllegalArgumentException.class, () -> account.verify(clock));

            assertFalse(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should verify when verification time equals update time")
        void shouldVerifyWhenVerificationTimeEqualsUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, false);

            account.verify(clock);

            assertTrue(account.isVerified());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

    }

    @Nested
    @DisplayName("Set deletion schedule tests")
    class SetDeletionScheduleTests
    {
        @Test
        @DisplayName("Should schedule deletion")
        void shouldScheduleDeletion ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            account.setDeletionSchedule(clock);

            assertEquals(UPDATE_TIME, account.getDeletionScheduledAt());
            assertEquals(UPDATE_TIME, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should do nothing when deletion is already scheduled")
        void shouldDoNothingWhenDeletionIsAlreadyScheduled ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            account.setDeletionSchedule(clock);

            assertEquals(UPDATED_AT, account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertThrows(NullPointerException.class, () -> account.setDeletionSchedule(null));

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when schedule time is before creation")
        void shouldFailWhenScheduleTimeIsBeforeCreation ()
        {
            Clock clock = Clock.fixed(CREATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertThrows(IllegalArgumentException.class, () -> account.setDeletionSchedule(clock));

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when schedule time is before update time")
        void shouldFailWhenScheduleTimeIsBeforeUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertThrows(IllegalArgumentException.class, () -> account.setDeletionSchedule(clock));

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should schedule deletion when schedule time equals update time")
        void shouldScheduleDeletionWhenScheduleTimeEqualsUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            account.setDeletionSchedule(clock);

            assertEquals(UPDATED_AT, account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

    }

    @Nested
    @DisplayName("Remove deletion schedule tests")
    class RemoveDeletionScheduleTests
    {
        @Test
        @DisplayName("Should remove deletion schedule")
        void shouldRemoveDeletionSchedule ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            account.removeDeletionSchedule(clock);

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATE_TIME, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should do nothing when deletion is not scheduled")
        void shouldDoNothingWhenDeletionIsNotScheduled ()
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            account.removeDeletionSchedule(clock);

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            assertThrows(NullPointerException.class, () -> account.removeDeletionSchedule(null));

            assertEquals(UPDATED_AT, account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when removal time is before creation")
        void shouldFailWhenRemovalTimeIsBeforeCreation ()
        {
            Clock clock = Clock.fixed(CREATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            assertThrows(IllegalArgumentException.class, () -> account.removeDeletionSchedule(clock));

            assertEquals(UPDATED_AT, account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should fail when removal time is before update time")
        void shouldFailWhenRemovalTimeIsBeforeUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            assertThrows(IllegalArgumentException.class, () -> account.removeDeletionSchedule(clock));

            assertEquals(UPDATED_AT, account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

        @Test
        @DisplayName("Should remove deletion schedule when removal time equals update time")
        void shouldRemoveDeletionScheduleWhenRemovalTimeEqualsUpdateTime ()
        {
            Clock clock = Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
            Account account = restore(ACCOUNT_ID, UPDATED_AT, UPDATED_AT, true);

            account.removeDeletionSchedule(clock);

            assertNull(account.getDeletionScheduledAt());
            assertEquals(UPDATED_AT, account.getUpdatedAt());
        }

    }

    @Nested
    @DisplayName("Update tests")
    class UpdateTests
    {


        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should update when verified")
        void shouldUpdateWhenVerified (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            updateCase.updateToNewValue(account, clock);

            assertEquals(UPDATE_TIME, account.getUpdatedAt());
            assertEquals(updateCase.newValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should update when unverified")
        void shouldUpdateWhenUnverified (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                false
            );

            updateCase.updateToNewValue(account, clock);

            assertEquals(UPDATE_TIME, account.getUpdatedAt());
            assertEquals(updateCase.newValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull (AccountFixtures.UpdateCase<?> updateCase)
        {
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            assertThrows(
                NullPointerException.class,
                () -> updateCase.updateToNewValue(account, null)
            );

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should fail when field is null")
        void shouldFailWhenFieldIsNull (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            assertThrows(
                NullPointerException.class,
                () -> updateCase.updateToNull(account, clock)
            );

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should fail when scheduled for deletion")
        void shouldFailWhenScheduledForDeletion (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                UPDATED_AT,
                true
            );

            assertThrows(
                AccountUpdateFailedException.class,
                () -> updateCase.updateToNewValue(account, clock)
            );

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should do nothing when field does not change")
        void shouldDoNothingWhenFieldDoesNotChange (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATE_TIME, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            updateCase.updateToOldValue(account, clock);

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should fail when update time is before created at")
        void shouldFailWhenUpdateTimeIsBeforeCreatedAt (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(CREATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> updateCase.updateToNewValue(account, clock)
            );

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should fail when update time is before updated at")
        void shouldFailWhenUpdateTimeIsBeforeUpdatedAt (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATED_AT.minusSeconds(1), ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> updateCase.updateToNewValue(account, clock)
            );

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.oldValue(), updateCase.readField(account));
        }

        @MethodSource(FIXTURES + "#getUpdateCases")
        @ParameterizedTest(name = "{argumentSetName}")
        @DisplayName("Should update when update time equals updated at")
        void shouldUpdateWhenUpdateTimeEqualsUpdatedAt (AccountFixtures.UpdateCase<?> updateCase)
        {
            Clock clock = Clock.fixed(UPDATED_AT, ZoneOffset.UTC);
            Account account = AccountFixtures.createAccount(
                CREATED_AT,
                UPDATED_AT,
                null,
                true
            );

            updateCase.updateToNewValue(account, clock);

            assertEquals(UPDATED_AT, account.getUpdatedAt());
            assertEquals(updateCase.newValue(), updateCase.readField(account));
        }
    }

    @Nested
    @DisplayName("Equals and hash code tests")
    class EqualsAndHashCodeTests
    {
        @Test
        @DisplayName("Should be equal when identifiers are equal")
        void shouldBeEqualWhenIdentifiersAreEqual ()
        {
            Account first = restore(ACCOUNT_ID, UPDATED_AT, null, true);
            Account second = Account.restore(
                ACCOUNT_ID,
                Email.restore("another@example.com"),
                EncodedPassword.from("another_encoded_password"),
                Role.ADMIN,
                CREATED_AT,
                UPDATE_TIME,
                null,
                true
            );

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when identifiers differ")
        void shouldNotBeEqualWhenIdentifiersDiffer ()
        {
            Account first = restore(ACCOUNT_ID, UPDATED_AT, null, true);
            Account second = restore(
                UUID.fromString("019b76da-a000-7000-8000-000000000002"),
                UPDATED_AT,
                null,
                true
            );

            assertNotEquals(first, second);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertEquals(account, account);
        }

        @Test
        @DisplayName("Should not be equal to null or another type")
        void shouldNotBeEqualToNullOrAnotherType ()
        {
            Account account = restore(ACCOUNT_ID, UPDATED_AT, null, true);

            assertNotEquals(null, account);
            assertNotEquals(ACCOUNT_ID, account);
        }

    }

    private static Account restore (
        UUID id,
        Instant updatedAt,
        Instant deletionScheduledAt,
        boolean verified
    )
    {
        return Account.restore(
            id,
            EMAIL,
            ENCODED_PASSWORD,
            Role.USER,
            CREATED_AT,
            updatedAt,
            deletionScheduledAt,
            verified
        );
    }
}
