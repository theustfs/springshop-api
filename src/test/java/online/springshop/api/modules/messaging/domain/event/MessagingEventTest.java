package online.springshop.api.modules.messaging.domain.event;

import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Messaging event tests")
class MessagingEventTest
{
    private static final IdempotencyKey ID = new IdempotencyKey(
        UUID.fromString("019aa1bb-2cc3-7dd4-8ee5-6ff778899001")
    );
    private static final Instant CREATED_AT = Instant.parse("2026-11-15T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-11-15T10:01:00Z");

    private static Clock clockAt (Instant instant)
    {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should reject a null id")
        void shouldRejectNullId ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MessagingEvent.from(null, clockAt(CREATED_AT))
            );
        }

        @Test
        @DisplayName("Should reject a null clock")
        void shouldRejectNullClock ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MessagingEvent.from(ID, null)
            );
        }

        @Test
        @DisplayName("Should start in created state with the clock time")
        void shouldCreateEventInCreatedState ()
        {
            MessagingEvent event = MessagingEvent.from(ID, clockAt(CREATED_AT));

            assertEquals(ID, event.getId());
            assertEquals(MessagingEventStatus.CREATED, event.getStatus());
            assertEquals(CREATED_AT, event.getCreatedAt());
            assertNull(event.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should reject null id")
        void shouldRejectNullId ()
        {
            assertThrows(
                NullPointerException.class, () -> MessagingEvent.restore(
                    null,
                    MessagingEventStatus.CREATED,
                    CREATED_AT,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should reject null status")
        void shouldRejectNullStatus ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MessagingEvent.restore(
                    ID,
                    null,
                    CREATED_AT,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should reject null creation time")
        void shouldRejectNullCreatedAt ()
        {
            assertThrows(
                NullPointerException.class,
                () -> MessagingEvent.restore(
                    ID,
                    MessagingEventStatus.CREATED,
                    null,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should reject update time before creation")
        void shouldRejectUpdatedAtBeforeCreatedAt ()
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> MessagingEvent.restore(
                    ID,
                    MessagingEventStatus.PROCESSING,
                    CREATED_AT,
                    CREATED_AT.minusNanos(1)
                )
            );
        }

        @Test
        @DisplayName("Should restore persisted state and timestamps")
        void shouldRestorePersistedState ()
        {
            MessagingEvent event = MessagingEvent.restore(
                ID, MessagingEventStatus.PROCESSING, CREATED_AT, UPDATED_AT
            );

            assertEquals(ID, event.getId());
            assertEquals(MessagingEventStatus.PROCESSING, event.getStatus());
            assertEquals(CREATED_AT, event.getCreatedAt());
            assertEquals(UPDATED_AT, event.getUpdatedAt());
        }

        @Test
        @DisplayName("Should allow an update at the creation time")
        void shouldAllowUpdatedAtEqualToCreatedAt ()
        {
            MessagingEvent event = MessagingEvent.restore(
                ID, MessagingEventStatus.PROCESSED, CREATED_AT, CREATED_AT
            );

            assertEquals(CREATED_AT, event.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Status update tests")
    class StatusUpdateTests
    {
        @Test
        @DisplayName("Should reject a null status without changing state")
        void shouldRejectNullStatus ()
        {
            MessagingEvent event = MessagingEvent.from(ID, clockAt(CREATED_AT));

            assertThrows(NullPointerException.class, () -> event.updateStatus(null, clockAt(UPDATED_AT)));
            assertEquals(MessagingEventStatus.CREATED, event.getStatus());
            assertNull(event.getUpdatedAt());
        }

        @Test
        @DisplayName("Should reject a null clock without changing state")
        void shouldRejectNullClock ()
        {
            MessagingEvent event = MessagingEvent.from(ID, clockAt(CREATED_AT));

            assertThrows(NullPointerException.class, () -> event.updateStatus(MessagingEventStatus.PROCESSING, null));
            assertEquals(MessagingEventStatus.CREATED, event.getStatus());
            assertNull(event.getUpdatedAt());
        }

        @ParameterizedTest
        @EnumSource(MessagingEventStatus.class)
        @DisplayName("Should record any requested status and current time")
        void shouldRecordRequestedStatus (MessagingEventStatus status)
        {
            MessagingEvent event = MessagingEvent.from(ID, clockAt(CREATED_AT));

            event.updateStatus(status, clockAt(UPDATED_AT));

            assertEquals(status, event.getStatus());
            assertEquals(UPDATED_AT, event.getUpdatedAt());
            assertEquals(CREATED_AT, event.getCreatedAt());
        }

        @Test
        @DisplayName("Should reject update time before creation without changing state")
        void shouldRejectUpdateBeforeCreation ()
        {
            MessagingEvent event = MessagingEvent.from(ID, clockAt(CREATED_AT));

            assertThrows(IllegalArgumentException.class, () -> event.updateStatus(
                MessagingEventStatus.FAILED, clockAt(CREATED_AT.minusNanos(1))
            ));
            assertEquals(MessagingEventStatus.CREATED, event.getStatus());
            assertNull(event.getUpdatedAt());
        }

        @Test
        @DisplayName("Should reject update time before previous update without changing state")
        void shouldRejectUpdateBeforePreviousUpdate ()
        {
            MessagingEvent event = MessagingEvent.restore(
                ID, MessagingEventStatus.PROCESSING, CREATED_AT, UPDATED_AT
            );

            assertThrows(IllegalArgumentException.class, () -> event.updateStatus(
                MessagingEventStatus.FAILED, clockAt(UPDATED_AT.minusNanos(1))
            ));
            assertEquals(MessagingEventStatus.PROCESSING, event.getStatus());
            assertEquals(UPDATED_AT, event.getUpdatedAt());
        }

        @Test
        @DisplayName("Should allow the update time to equal the previous update")
        void shouldAllowEqualUpdateTime ()
        {
            MessagingEvent event = MessagingEvent.restore(
                ID, MessagingEventStatus.PROCESSING, CREATED_AT, UPDATED_AT
            );

            event.updateStatus(MessagingEventStatus.PROCESSED, clockAt(UPDATED_AT));

            assertEquals(MessagingEventStatus.PROCESSED, event.getStatus());
            assertEquals(UPDATED_AT, event.getUpdatedAt());
        }
    }
}
