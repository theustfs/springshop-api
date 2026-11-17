package online.springshop.api.modules.messaging.infrastructure.persistence.mapper;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryId;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryStatus;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MailDeliveryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail delivery mapper tests")
class MailDeliveryMapperTest
{
    private final MailDeliveryMapper mapper = Mappers.getMapper(MailDeliveryMapper.class);

    @Nested
    @DisplayName("Entity to domain mapping tests")
    class EntityToDomainMappingTests
    {
        @Test
        @DisplayName("Should map entity to domain")
        void shouldMapEntityToDomain ()
        {
            Instant now = Instant.now();

            MailDeliveryEntity entity = new MailDeliveryEntity(
                Generators.timeBasedEpochGenerator().generate(),
                Generators.timeBasedEpochGenerator().generate(),
                MailDeliveryStatus.SENT,
                now,
                now.plusSeconds(10),
                now.plusSeconds(15)
            );

            MailDelivery domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(entity.getId(), domain.getId().value());
            assertEquals(entity.getProviderId(), domain.getProviderId().value());
            assertEquals(entity.getStatus(), domain.getStatus());
            assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
            assertEquals(entity.getStatusChangedAt(), domain.getStatusChangedAt());
            assertEquals(entity.getProviderStatusChangedAt(), domain.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull ()
        {
            assertNull(mapper.toDomain(null));
        }
    }

    @Nested
    @DisplayName("Domain to entity mapping tests")
    class DomainToEntityMappingTests
    {
        @Test
        @DisplayName("Should map domain to entity")
        void shouldMapDomainToEntity ()
        {
            Instant now = Instant.now();

            MailDelivery domain = MailDelivery.restore(
                new MailDeliveryId(Generators.timeBasedEpochGenerator().generate()),
                new MailProviderId(Generators.timeBasedEpochGenerator().generate()),
                MailDeliveryStatus.SENT,
                now,
                now.plusSeconds(10),
                now.plusSeconds(15)
            );

            MailDeliveryEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(domain.getId().value(), entity.getId());
            assertEquals(domain.getProviderId().value(), entity.getProviderId());
            assertEquals(domain.getStatus(), entity.getStatus());
            assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
            assertEquals(domain.getStatusChangedAt(), entity.getStatusChangedAt());
            assertEquals(domain.getProviderStatusChangedAt(), entity.getProviderStatusChangedAt());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull ()
        {
            assertNull(mapper.toEntity(null));
        }
    }
}
