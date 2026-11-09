package online.springshop.api.modules.auth.application.port;

import online.springshop.api.modules.auth.domain.useraction.UserAction;

import java.util.Optional;
import java.util.UUID;

/**
 * Application-facing persistence contract for {@link UserAction} aggregates.
 *
 * <p>Locking lookups acquire a database write lock and must be invoked inside an
 * application-service transaction. The lock remains effective until that
 * surrounding transaction completes.</p>
 */
public interface UserActionRepository
{
    /** Saves a new aggregate or persists changes to an existing aggregate. */
    void save (UserAction userAction);

    /** Finds an aggregate by its user-action identifier. */
    Optional<UserAction> findById (UUID id);

    /** Finds the single current aggregate owned by an account. */
    Optional<UserAction> findByAccountId (UUID accountId);

    /** Finds an aggregate by ID and acquires a pessimistic write lock. */
    Optional<UserAction> findByIdAndLock (UUID id);

    /** Finds an aggregate by account ID and acquires a pessimistic write lock. */
    Optional<UserAction> findByAccountIdAndLock (UUID accountId);

    /** Deletes an aggregate by its user-action identifier. */
    void deleteById (UUID id);

    /** Deletes the current aggregate owned by an account. */
    void deleteByAccountId (UUID accountId);
}
