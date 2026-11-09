package online.springshop.api.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.auth.application.port.UserActionRepository;
import online.springshop.api.modules.auth.domain.useraction.UserAction;
import online.springshop.api.modules.auth.infrastructure.persistence.mapper.UserActionMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA implementation of the application-facing
 * {@link UserActionRepository} contract.
 *
 * <p>Transaction boundaries remain the responsibility of application
 * services, particularly when a pessimistic-locking lookup is used.</p>
 */
@Component
@RequiredArgsConstructor
public class UserActionJpaRepositoryAdapter implements UserActionRepository
{
    private final UserActionJpaRepository repository;
    private final UserActionMapper mapper;

    @Override
    public void save (UserAction userAction)
    {
        repository.save(mapper.toEntity(userAction));
    }

    @Override
    public Optional<UserAction> findById (UUID id)
    {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<UserAction> findByAccountId (UUID accountId)
    {
        return repository.findByAccountId(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<UserAction> findByIdAndLock (UUID id)
    {
        return repository.findByIdAndLock(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<UserAction> findByAccountIdAndLock (UUID accountId)
    {
        return repository.findByAccountIdAndLock(accountId)
            .map(mapper::toDomain);
    }

    @Override
    public void deleteById (UUID id)
    {
        repository.deleteById(id);
    }

    @Override
    public void deleteByAccountId (UUID accountId)
    {
        repository.deleteByAccountId(accountId);
    }
}
