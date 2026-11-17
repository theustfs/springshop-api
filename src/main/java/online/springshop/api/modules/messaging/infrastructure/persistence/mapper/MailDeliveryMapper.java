package online.springshop.api.modules.messaging.infrastructure.persistence.mapper;

import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryId;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.infrastructure.persistence.entity.MailDeliveryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MailDeliveryMapper
{
    default MailDeliveryEntity toEntity (MailDelivery domain)
    {
        if (domain == null)
            return null;

        return new MailDeliveryEntity(
            domain.getId().value(),
            domain.getProviderId().value(),
            domain.getStatus(),
            domain.getCreatedAt(),
            domain.getStatusChangedAt(),
            domain.getProviderStatusChangedAt()
        );
    }

    default MailDelivery toDomain (MailDeliveryEntity entity)
    {
        if (entity == null)
            return null;

        return MailDelivery.restore(
            new MailDeliveryId(entity.getId()),
            new MailProviderId(entity.getProviderId()),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getStatusChangedAt(),
            entity.getProviderStatusChangedAt()
        );
    }

    // VOs mapping
    default UUID map (MailDeliveryId mailDeliveryId)
    {
        return mailDeliveryId.value();
    }

    default UUID map (MailProviderId mailProviderId)
    {
        return mailProviderId.value();
    }
}
