package online.springshop.api.modules.auth.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.auth.application.port.AccountRepository;
import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.infrastructure.persistence.mapper.AccountMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepositoryJpaAdapter implements AccountRepository
{
    private final AccountJpaRepository repository;
    private final AccountMapper mapper;

    @Override
    public void save (Account account)
    {
        repository.save(mapper.toEntity(account));
    }

    @Override
    public Optional<Account> findById (UUID id)
    {
        return repository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail (Email email)
    {
        return repository.findByEmail(email.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByIdAndLock (UUID id)
    {
        return repository.findByIdAndLock(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmailAndLock (Email email)
    {
        return repository.findByEmailAndLock(email.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsById (UUID id)
    {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByEmail (Email email)
    {
        return repository.existsByEmail(email.getValue());
    }

    @Override
    public int deleteUnverifiedAccounts (Instant threshold, int batchSize)
    {
        return repository.deleteUnverifiedAccounts(threshold, batchSize);
    }

    @Override
    public int deleteAccountsScheduledForDeletion (Instant threshold, int batchSize)
    {
        return repository.deleteAccountsScheduledForDeletion(threshold, batchSize);
    }
}
