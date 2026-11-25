package online.springshop.api.modules.messaging.infrastructure.persistence.mapper;

import online.springshop.api.modules.messaging.domain.event.MessagingEvent;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MessagingEventEntity;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/** Converts messaging event state between the domain model and its JPA representation. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessagingEventMapper
{
    /**
     * Restores the domain event from a database entity.
     * @param entity persisted event, or {@code null}
     * @return restored event, or {@code null} when the entity is {@code null}
     */
    default MessagingEvent toDomain (MessagingEventEntity entity)
    {
        if (entity == null)
            return null;

        return MessagingEvent.restore(
            new IdempotencyKey(entity.getId()),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Maps a domain event to its persistence representation.
     * @param domain event to map
     * @return entity for persistence
     */
    default MessagingEventEntity toEntity (MessagingEvent domain)
    {
        if (domain == null)
            return null;

        return new MessagingEventEntity(
            domain.getId().value(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }

    /**
     * Maps the idempotency key to its UUID database representation.
     * @param id key to map
     * @return wrapped UUID
     */
    default UUID map (IdempotencyKey id)
    {
        return id.value();
    }
}
