package online.springshop.api.modules.auth.application.port;

import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Application-facing persistence contract for {@link Account} aggregates.
 *
 * <p>Implementations persist and restore complete aggregate state. Callers own
 * application transaction boundaries; in particular, pessimistic-locking and
 * bulk-deletion operations must execute inside a transaction that remains open
 * for the complete use case.</p>
 *
 * <p>Cleanup operations use an exclusive threshold and delete at most the
 * requested batch size.</p>
 */
public interface AccountRepository
{
    /**
     * Persists the complete current state of an account.
     *
     * @param account the account to persist
     */
    void save (Account account);

    /**
     * Finds an account by its stable identifier without acquiring a
     * pessimistic lock.
     *
     * @param id the account identifier
     * @return the account, or an empty result when it does not exist
     */
    Optional<Account> findById (UUID id);

    /**
     * Finds an account by its normalized email address without acquiring a
     * pessimistic lock.
     *
     * @param email the normalized email address
     * @return the account, or an empty result when it does not exist
     */
    Optional<Account> findByEmail (Email email);

    /**
     * Finds an account by its identifier and acquires a pessimistic write lock
     * on its persistence row.
     *
     * <p>The lock is held until the surrounding transaction completes.</p>
     *
     * @param id the account identifier
     * @return the locked account, or an empty result when it does not exist
     */
    Optional<Account> findByIdAndLock (UUID id);

    /**
     * Finds an account by its normalized email address and acquires a
     * pessimistic write lock on its persistence row.
     *
     * <p>The lock is held until the surrounding transaction completes.</p>
     *
     * @param email the normalized email address
     * @return the locked account, or an empty result when it does not exist
     */
    Optional<Account> findByEmailAndLock (Email email);

    /**
     * Reports whether an account exists for the supplied identifier.
     *
     * @param id the account identifier
     * @return {@code true} when the account exists
     */
    boolean existsById (UUID id);

    /**
     * Reports whether an account exists for the supplied normalized email.
     *
     * @param email the normalized email address
     * @return {@code true} when the account exists
     */
    boolean existsByEmail (Email email);

    /**
     * Deletes a bounded batch of accounts whose deletion was scheduled before
     * the exclusive threshold. Rows locked by another transaction are skipped.
     *
     * @param threshold exclusive deletion-schedule threshold
     * @param batchSize maximum number of accounts to delete; must be positive
     * @return the number of deleted accounts
     */
    int deleteAccountsScheduledForDeletion (Instant threshold, int batchSize);

    /**
     * Deletes a bounded batch of accounts created before the exclusive
     * threshold that remain unverified. Rows locked by another transaction are
     * skipped, and a deletion schedule does not make an expired unverified
     * account ineligible.
     *
     * @param threshold exclusive account-creation threshold
     * @param batchSize maximum number of accounts to delete; must be positive
     * @return the number of deleted accounts
     */
    int deleteUnverifiedAccounts (Instant threshold, int batchSize);
}
