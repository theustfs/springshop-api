package online.springshop.api.modules.messaging.domain.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail delivery tests")
class MailDeliveryTest
{
    private static final MailDeliveryId DELIVERY_ID = new MailDeliveryId(
        UUID.fromString("019aa1bb-2cc3-7dd4-8ee5-6ff778899001")
    );
    private static final MailDeliveryId OTHER_DELIVERY_ID = new MailDeliveryId(
        UUID.fromString("019aa1bb-2cc3-7dd4-8ee5-6ff778899002")
    );
    private static final MailProviderId PROVIDER_ID = new MailProviderId(
        UUID.fromString("019aa1bb-2cc3-7dd4-8ee5-6ff778899003")
    );
    private static final MailProviderId OTHER_PROVIDER_ID = new MailProviderId(
        UUID.fromString("019aa1bb-2cc3-7dd4-8ee5-6ff778899004")
    );

    private static final Instant CREATED_AT = Instant.parse("2026-11-15T10:00:00Z");
    private static final Instant STATUS_CHANGED_AT = Instant.parse("2026-11-15T10:01:00Z");
    private static final Instant UPDATE_TIME = Instant.parse("2026-11-15T10:02:00Z");
    private static final Instant PROVIDER_STATUS_CHANGED_AT = Instant.parse("2026-11-15T09:59:00Z");
    private static final Instant NEW_PROVIDER_STATUS_CHANGED_AT = Instant.parse("2026-11-15T10:01:30Z");

    private static Clock clockAt (Instant instant)
    {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private static MailDelivery restoreInStatus (MailDeliveryStatus status)
    {
        if (status == MailDeliveryStatus.SUBMITTED)
        {
            return MailDelivery.restore(
                DELIVERY_ID,
                PROVIDER_ID,
                status,
                CREATED_AT,
                null,
                null
            );
        }

        return MailDelivery.restore(
            DELIVERY_ID,
            PROVIDER_ID,
            status,
            CREATED_AT,
            STATUS_CHANGED_AT,
            PROVIDER_STATUS_CHANGED_AT
        );
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should fail when provider id is null")
        void shouldFailWhenProviderIdIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.from(null, clockAt(CREATED_AT))
            );
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.from(PROVIDER_ID, null)
            );
        }

        @Test
        @DisplayName("Should create submitted mail delivery")
        void shouldCreateSubmittedMailDelivery ()
        {
            MailDelivery delivery = MailDelivery.from(PROVIDER_ID, clockAt(CREATED_AT));

            assertNotNull(delivery.getId());
            assertEquals(PROVIDER_ID, delivery.getProviderId());
            assertEquals(MailDeliveryStatus.SUBMITTED, delivery.getStatus());
            assertEquals(CREATED_AT, delivery.getCreatedAt());
            assertNull(delivery.getStatusChangedAt());
            assertNull(delivery.getProviderStatusChangedAt());
        }
    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should fail when delivery id is null")
        void shouldFailWhenDeliveryIdIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.restore(
                    null,
                    PROVIDER_ID,
                    MailDeliveryStatus.SUBMITTED,
                    CREATED_AT,
                    null,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should fail when provider id is null")
        void shouldFailWhenProviderIdIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.restore(
                    DELIVERY_ID,
                    null,
                    MailDeliveryStatus.SUBMITTED,
                    CREATED_AT,
                    null,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should fail when status is null")
        void shouldFailWhenStatusIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.restore(
                    DELIVERY_ID,
                    PROVIDER_ID,
                    null,
                    CREATED_AT,
                    null,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should fail when created at is null")
        void shouldFailWhenCreatedAtIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MailDelivery.restore(
                    DELIVERY_ID,
                    PROVIDER_ID,
                    MailDeliveryStatus.SUBMITTED,
                    null,
                    null,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should fail when changed status has no local change time")
        void shouldFailWhenChangedStatusHasNoLocalChangeTime ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> MailDelivery.restore(
                    DELIVERY_ID,
                    PROVIDER_ID,
                    MailDeliveryStatus.DELIVERED,
                    CREATED_AT,
                    null,
                    PROVIDER_STATUS_CHANGED_AT
                )
            );
        }

        @Test
        @DisplayName("Should fail when status changed before creation")
        void shouldFailWhenStatusChangedBeforeCreation ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> MailDelivery.restore(
                    DELIVERY_ID,
                    PROVIDER_ID,
                    MailDeliveryStatus.SENT,
                    CREATED_AT,
                    CREATED_AT.minusSeconds(1),
                    PROVIDER_STATUS_CHANGED_AT
                )
            );
        }

        @Test
        @DisplayName("Should restore submitted mail delivery")
        void shouldRestoreSubmittedMailDelivery ()
        {
            MailDelivery delivery = MailDelivery.restore(
                DELIVERY_ID,
                PROVIDER_ID,
                MailDeliveryStatus.SUBMITTED,
                CREATED_AT,
                null,
                null
            );

            assertEquals(DELIVERY_ID, delivery.getId());
            assertEquals(PROVIDER_ID, delivery.getProviderId());
            assertEquals(MailDeliveryStatus.SUBMITTED, delivery.getStatus());
            assertEquals(CREATED_AT, delivery.getCreatedAt());
            assertNull(delivery.getStatusChangedAt());
            assertNull(delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should restore updated mail delivery")
        void shouldRestoreUpdatedMailDelivery ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.DELIVERED);

            assertEquals(DELIVERY_ID, delivery.getId());
            assertEquals(PROVIDER_ID, delivery.getProviderId());
            assertEquals(MailDeliveryStatus.DELIVERED, delivery.getStatus());
            assertEquals(CREATED_AT, delivery.getCreatedAt());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }
    }

    @Nested
    @DisplayName("Status update tests")
    class StatusUpdateTests
    {
        static Stream<Arguments> validTransitions ()
        {
            return Stream.of(
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.SENT),
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.DELIVERY_DELAYED),
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.DELIVERED),
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.BOUNCED),
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.FAILED),
                Arguments.of(MailDeliveryStatus.SUBMITTED, MailDeliveryStatus.SUPPRESSED),
                Arguments.of(MailDeliveryStatus.SENT, MailDeliveryStatus.DELIVERED),
                Arguments.of(MailDeliveryStatus.SENT, MailDeliveryStatus.BOUNCED),
                Arguments.of(MailDeliveryStatus.SENT, MailDeliveryStatus.FAILED),
                Arguments.of(MailDeliveryStatus.DELIVERY_DELAYED, MailDeliveryStatus.DELIVERED),
                Arguments.of(MailDeliveryStatus.DELIVERY_DELAYED, MailDeliveryStatus.BOUNCED),
                Arguments.of(MailDeliveryStatus.DELIVERY_DELAYED, MailDeliveryStatus.FAILED)
            );
        }

        @Test
        @DisplayName("Should fail when new status is null")
        void shouldFailWhenNewStatusIsNull ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SUBMITTED);

            assertThrows(
                NullPointerException.class,
                () -> delivery.updateStatus(null, NEW_PROVIDER_STATUS_CHANGED_AT, clockAt(UPDATE_TIME))
            );
        }

        @Test
        @DisplayName("Should fail when provider status changed at is null")
        void shouldFailWhenProviderStatusChangedAtIsNull ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SUBMITTED);

            assertThrows(
                NullPointerException.class,
                () -> delivery.updateStatus(MailDeliveryStatus.SENT, null, clockAt(UPDATE_TIME))
            );
        }

        @Test
        @DisplayName("Should fail when clock is null")
        void shouldFailWhenClockIsNull ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SUBMITTED);

            assertThrows(
                NullPointerException.class,
                () -> delivery.updateStatus(MailDeliveryStatus.SENT, NEW_PROVIDER_STATUS_CHANGED_AT, null)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("validTransitions")
        @DisplayName("Should apply valid status transition")
        void shouldApplyValidStatusTransition (
            MailDeliveryStatus currentStatus,
            MailDeliveryStatus newStatus
        )
        {
            MailDelivery delivery = restoreInStatus(currentStatus);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                newStatus,
                NEW_PROVIDER_STATUS_CHANGED_AT,
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.APPLIED, result);
            assertEquals(newStatus, delivery.getStatus());
            assertEquals(UPDATE_TIME, delivery.getStatusChangedAt());
            assertEquals(NEW_PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should ignore stale provider event without changing delivery")
        void shouldIgnoreStaleProviderEventWithoutChangingDelivery ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SENT);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                MailDeliveryStatus.DELIVERED,
                PROVIDER_STATUS_CHANGED_AT.minusSeconds(1),
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.STALE, result);
            assertEquals(MailDeliveryStatus.SENT, delivery.getStatus());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should report conflicting status at the same provider time without changing delivery")
        void shouldReportConflictingStatusAtSameProviderTimeWithoutChangingDelivery ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SENT);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                MailDeliveryStatus.DELIVERED,
                PROVIDER_STATUS_CHANGED_AT,
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.CONFLICT, result);
            assertEquals(MailDeliveryStatus.SENT, delivery.getStatus());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should ignore duplicate status without changing delivery")
        void shouldIgnoreDuplicateStatusWithoutChangingDelivery ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SENT);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                MailDeliveryStatus.SENT,
                NEW_PROVIDER_STATUS_CHANGED_AT,
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.DUPLICATE, result);
            assertEquals(MailDeliveryStatus.SENT, delivery.getStatus());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }

        @ParameterizedTest
        @EnumSource(value = MailDeliveryStatus.class, names = {
            "DELIVERED",
            "BOUNCED",
            "FAILED",
            "SUPPRESSED"
        })
        @DisplayName("Should ignore updates after terminal status")
        void shouldIgnoreUpdatesAfterTerminalStatus (MailDeliveryStatus terminalStatus)
        {
            MailDelivery delivery = restoreInStatus(terminalStatus);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                MailDeliveryStatus.SENT,
                NEW_PROVIDER_STATUS_CHANGED_AT,
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.IGNORED_REGRESSION, result);
            assertEquals(terminalStatus, delivery.getStatus());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should ignore invalid non terminal regression")
        void shouldIgnoreInvalidNonTerminalRegression ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.DELIVERY_DELAYED);

            MailDeliveryUpdateResult result = delivery.updateStatus(
                MailDeliveryStatus.SENT,
                NEW_PROVIDER_STATUS_CHANGED_AT,
                clockAt(UPDATE_TIME)
            );

            assertEquals(MailDeliveryUpdateResult.IGNORED_REGRESSION, result);
            assertEquals(MailDeliveryStatus.DELIVERY_DELAYED, delivery.getStatus());
        }

        @Test
        @DisplayName("Should fail when update time is before creation")
        void shouldFailWhenUpdateTimeIsBeforeCreation ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SUBMITTED);

            assertThrows(
                IllegalArgumentException.class,
                () -> delivery.updateStatus(
                    MailDeliveryStatus.SENT,
                    NEW_PROVIDER_STATUS_CHANGED_AT,
                    clockAt(CREATED_AT.minusSeconds(1))
                )
            );

            assertEquals(MailDeliveryStatus.SUBMITTED, delivery.getStatus());
            assertNull(delivery.getStatusChangedAt());
            assertNull(delivery.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should fail when update time is before latest local status change")
        void shouldFailWhenUpdateTimeIsBeforeLatestLocalStatusChange ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SENT);

            assertThrows(
                IllegalArgumentException.class,
                () -> delivery.updateStatus(
                    MailDeliveryStatus.DELIVERED,
                    NEW_PROVIDER_STATUS_CHANGED_AT,
                    clockAt(STATUS_CHANGED_AT.minusSeconds(1))
                )
            );

            assertEquals(MailDeliveryStatus.SENT, delivery.getStatus());
            assertEquals(STATUS_CHANGED_AT, delivery.getStatusChangedAt());
            assertEquals(PROVIDER_STATUS_CHANGED_AT, delivery.getProviderStatusChangedAt());
        }
    }

    @Nested
    @DisplayName("Terminal status report tests")
    class TerminalStatusReportTests
    {
        @EnumSource(value = MailDeliveryStatus.class, names = {
            "DELIVERED",
            "BOUNCED",
            "FAILED",
            "SUPPRESSED"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should return true when status is terminal")
        void shouldReturnTrueWhenStatusIsTerminal (MailDeliveryStatus status)
        {
            MailDelivery mailDelivery = restoreInStatus(status);

            assertTrue(mailDelivery.isStatusTerminal());
        }

        @EnumSource(value = MailDeliveryStatus.class, names = {
            "SUBMITTED",
            "SENT",
            "DELIVERY_DELAYED"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should return false when status is not terminal")
        void shouldReturnFalseWhenStatusIsNotTerminal (MailDeliveryStatus status)
        {
            MailDelivery mailDelivery = restoreInStatus(status);

            assertFalse(mailDelivery.isStatusTerminal());
        }
    }

    @Nested
    @DisplayName("Identity tests")
    class IdentityTests
    {
        @Test
        @DisplayName("Should be equal when delivery ids are equal")
        void shouldBeEqualWhenDeliveryIdsAreEqual ()
        {
            MailDelivery delivery1 = restoreInStatus(MailDeliveryStatus.SUBMITTED);
            MailDelivery delivery2 = MailDelivery.restore(
                DELIVERY_ID,
                OTHER_PROVIDER_ID,
                MailDeliveryStatus.DELIVERED,
                CREATED_AT,
                STATUS_CHANGED_AT,
                PROVIDER_STATUS_CHANGED_AT
            );

            assertEquals(delivery1, delivery2);
            assertEquals(delivery1.hashCode(), delivery2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when delivery ids are different")
        void shouldNotBeEqualWhenDeliveryIdsAreDifferent ()
        {
            MailDelivery delivery1 = restoreInStatus(MailDeliveryStatus.SUBMITTED);
            MailDelivery delivery2 = MailDelivery.restore(
                OTHER_DELIVERY_ID,
                PROVIDER_ID,
                MailDeliveryStatus.SUBMITTED,
                CREATED_AT,
                null,
                null
            );

            assertNotEquals(delivery1, delivery2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNullOrAnotherType ()
        {
            MailDelivery delivery = restoreInStatus(MailDeliveryStatus.SUBMITTED);

            assertNotEquals(null, delivery);
        }
    }
}
