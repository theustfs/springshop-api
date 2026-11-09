package online.springshop.api.modules.auth.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.UserActionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data access to user-action persistence entities. */
@Repository
public interface UserActionJpaRepository extends JpaRepository<UserActionEntity, UUID>
{
    Optional<UserActionEntity> findByAccountId (UUID accountId);

    /**
     * Loads the complete aggregate and locks its action row for mutation by the
     * current transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "challenges")
    @Query("select uae from UserActionEntity uae where uae.id = :id")
    Optional<UserActionEntity> findByIdAndLock (UUID id);

    /**
     * Loads the complete aggregate for an account and locks its action row for
     * mutation by the current transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "challenges")
    @Query("select uae from UserActionEntity uae where uae.accountId = :accountId")
    Optional<UserActionEntity> findByAccountIdAndLock (UUID accountId);

    void deleteByAccountId (UUID accountId);
}
