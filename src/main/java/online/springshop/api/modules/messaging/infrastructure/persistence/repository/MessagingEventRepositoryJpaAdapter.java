package online.springshop.api.modules.messaging.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.messaging.application.port.MessagingEventRepository;
import online.springshop.api.modules.messaging.domain.event.MessagingEvent;
import online.springshop.api.modules.messaging.infrastructure.persistence.mapper.MessagingEventMapper;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/** Implements the messaging event persistence port using Spring Data JPA. */
@Component
@RequiredArgsConstructor
public class MessagingEventRepositoryJpaAdapter implements MessagingEventRepository
{
    private final MessagingEventJpaRepository repository;
    private final MessagingEventMapper mapper;

    @Override
    public void save (MessagingEvent messagingEvent)
    {
        repository.save(mapper.toEntity(messagingEvent));
    }

    @Override
    public boolean saveAndClaim (MessagingEvent messagingEvent)
    {
        return repository.saveAndClaim(mapper.toEntity(messagingEvent)) == 1;
    }

    @Override
    public Optional<MessagingEvent> findById (IdempotencyKey id)
    {
        return repository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<MessagingEvent> findByIdAndLock (IdempotencyKey id)
    {
        return repository.findByIdAndLock(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public void deleteOldRecords(Instant threshold, int batchSize)
    {
        repository.deleteOldRecords(threshold, batchSize);
    }
}
