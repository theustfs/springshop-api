package online.springshop.api.modules.auth.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID>
{
    Optional<AccountEntity> findByEmail (String email);

    boolean existsByEmail (String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT a FROM AccountEntity a
    WHERE a.id = :id
    """)
    Optional<AccountEntity> findByIdAndLock (UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT a FROM AccountEntity a
    WHERE a.email = :email
    """)
    Optional<AccountEntity> findByEmailAndLock (String email);

    @Modifying
    @Query(value = """
    DELETE FROM accounts WHERE id IN
    (
        SELECT id FROM accounts
        WHERE verified IS FALSE
        AND created_at < :threshold
        ORDER BY created_at, id
        FOR UPDATE SKIP LOCKED
        LIMIT :batchSize
    )
    """,
    nativeQuery = true)
    int deleteUnverifiedAccounts (Instant threshold, int batchSize);

    @Modifying
    @Query(value = """
    DELETE FROM accounts WHERE id IN
    (
        SELECT id FROM accounts
        WHERE deletion_scheduled_at IS NOT NULL
        AND deletion_scheduled_at < :threshold
        ORDER BY deletion_scheduled_at, id
        FOR UPDATE SKIP LOCKED
        LIMIT :batchSize
    )
    """,
    nativeQuery = true)
    int deleteAccountsScheduledForDeletion (Instant threshold, int batchSize);
}
