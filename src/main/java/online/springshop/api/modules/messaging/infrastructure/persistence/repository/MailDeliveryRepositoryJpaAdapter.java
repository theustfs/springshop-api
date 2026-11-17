package online.springshop.api.modules.messaging.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.messaging.application.port.MailDeliveryRepository;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailDeliveryId;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.infrastructure.persistence.mapper.MailDeliveryMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MailDeliveryRepositoryJpaAdapter implements MailDeliveryRepository
{
    private final MailDeliveryJpaRepository repository;
    private final MailDeliveryMapper mapper;

    @Override
    public void save (MailDelivery mailDelivery)
    {
        repository.save(mapper.toEntity(mailDelivery));
    }

    @Override
    public Optional<MailDelivery> findById (MailDeliveryId id)
    {
        return repository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<MailDelivery> findByProviderId (MailProviderId providerId)
    {
        return repository.findByProviderId(providerId.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<MailDelivery> findByIdAndLock (MailDeliveryId id)
    {
        return repository.findByIdAndLock(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<MailDelivery> findByProviderIdAndLock (MailProviderId providerId)
    {
        return repository.findByProviderIdAndLock(providerId.value())
            .map(mapper::toDomain);
    }

    @Override
    public void delete (MailDeliveryId id)
    {
        repository.deleteById(id.value());
    }

    @Override
    public void delete( MailProviderId providerId)
    {
        repository.deleteByProviderId(providerId.value());
    }
}
