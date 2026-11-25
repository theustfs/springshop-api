package online.springshop.api.modules.messaging.infrastructure.persistence.mapper;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.messaging.domain.event.MessagingEvent;
import online.springshop.api.modules.messaging.domain.event.MessagingEventStatus;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MessagingEventEntity;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Messaging event mapper tests")
class MessagingEventMapperTest
{
    private final MessagingEventMapper mapper = Mappers.getMapper(MessagingEventMapper.class);

    @Nested
    @DisplayName("Domain to entity mapping tests")
    class DomainToEntityMappingTests
    {
        @Test
        @DisplayName("Should map domain to entity")
        void shouldMapDomainToEntity ()
        {
            Instant now = Instant.now();

            MessagingEvent domain = MessagingEvent.restore(
                new IdempotencyKey(Generators.timeBasedEpochGenerator().generate()),
                MessagingEventStatus.PROCESSING,
                now,
                now.plusSeconds(60)
            );

            MessagingEventEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(domain.getId().value(), entity.getId());
            assertEquals(domain.getStatus(), entity.getStatus());
            assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
            assertEquals(domain.getUpdatedAt(), entity.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull ()
        {
            assertNull(mapper.toEntity(null));
        }
    }

    @Nested
    @DisplayName("Entity to domain mapping tests")
    class EntityToDomainMappingTests
    {
        @Test
        @DisplayName("Should map entity to domain")
        void shouldMapEntityToDomain ()
        {
            Instant now = Instant.now();

            MessagingEventEntity entity = new MessagingEventEntity(
                Generators.timeBasedEpochGenerator().generate(),
                MessagingEventStatus.PROCESSED,
                now,
                now.plusSeconds(30)
            );

            MessagingEvent domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(entity.getId(), domain.getId().value());
            assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
            assertEquals(entity.getUpdatedAt(), domain.getUpdatedAt());
            assertEquals(entity.getUpdatedAt(), domain.getUpdatedAt());
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull ()
        {
            assertNull(mapper.toDomain(null));
        }
    }
}