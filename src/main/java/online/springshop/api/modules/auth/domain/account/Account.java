package online.springshop.api.modules.auth.domain.account;

import com.fasterxml.uuid.Generators;
import lombok.Getter;
import online.springshop.api.modules.auth.domain.account.exception.UserUpdateFailedException;
import online.springshop.api.modules.auth.domain.account.exception.UserVerificationFailedException;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing a registered authentication identity.
 *
 * <p>The aggregate owns the user's identity, encoded credentials, authorization
 * role, email-verification state, and lifecycle timestamps. New users receive
 * {@link Role#USER} and begin unverified. A role describes authorization and is
 * independent of whether the user's email has been verified.</p>
 *
 * <p>Authorization, reauthentication, and confirmation challenges are
 * application-service responsibilities. In particular, callers must complete
 * the appropriate challenge before installing a sensitive value such as a new
 * email address or encoded password. The aggregate enforces its state and time
 * invariants after that authorization has succeeded.</p>
 *
 * <p>Ordinary updates and verification are rejected while deletion is
 * scheduled. Cancelling the deletion schedule remains available. Every
 * state-changing operation receives a {@link Clock}; mutation times may equal,
 * but must not precede, the aggregate's existing timestamps.</p>
 */
@Getter
public final class Account
{
    private final UUID id;

    private Email email;
    private EncodedPassword encodedPassword;
    private Role role;

    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletionScheduledAt;

    private boolean verified;

    private Account(
        UUID id,
        Email email,
        EncodedPassword encodedPassword,
        Role role,
        Instant createdAt,
        Instant updatedAt,
        Instant deletionScheduledAt,
        boolean verified
    )
    {
        this.id = Objects.requireNonNull(id, "Id must not be null");
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.encodedPassword = Objects.requireNonNull(encodedPassword, "Encoded password must not be null");
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at must not be null");
        this.updatedAt = updatedAt;
        this.deletionScheduledAt = deletionScheduledAt;
        this.verified = verified;
    }

    /**
     * Creates a new unverified user with the default {@link Role#USER} role.
     *
     * @param email the validated email address
     * @param encodedPassword the externally encoded password
     * @param clock the source of the creation time
     * @return a newly registered user
     * @throws NullPointerException when any argument is {@code null}
     */
    public static Account of (
        Email email,
        EncodedPassword encodedPassword,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        UUID id = Generators.timeBasedEpochGenerator().generate();
        Role role = Role.USER;
        Instant createdAt = clock.instant();
        boolean verified = false;

        return new Account(
            id,
            email,
            encodedPassword,
            role,
            createdAt,
            null,
            null,
            verified
        );
    }

    /**
     * Restores a user from trusted persisted state.
     *
     * <p>The update time must not precede the creation time. When deletion is
     * scheduled, its timestamp must equal the update time. A verified user must
     * have an update time recording a mutation after registration.</p>
     *
     * @param id the stable user identifier
     * @param email the restored email address
     * @param encodedPassword the restored encoded password
     * @param role the restored authorization role
     * @param createdAt the registration time
     * @param updatedAt the most recent mutation time, or {@code null}
     * @param deletionScheduledAt the deletion-scheduling time, or {@code null}
     * @param verified whether the current email is verified
     * @return the restored user
     * @throws NullPointerException when required state is {@code null}
     * @throws IllegalArgumentException when timestamp invariants are violated
     */
    public static Account restore (
        UUID id,
        Email email,
        EncodedPassword encodedPassword,
        Role role,
        Instant createdAt,
        Instant updatedAt,
        Instant deletionScheduledAt,
        boolean verified
    )
    {
        if (deletionScheduledAt != null && deletionScheduledAt.isBefore(createdAt))
            throw new IllegalArgumentException("Deletion scheduled at must not be before created at");

        if (deletionScheduledAt != null && updatedAt == null)
            throw new IllegalArgumentException("Updated at must not be null when deletion scheduled at is set");

        if (deletionScheduledAt != null && updatedAt.compareTo(deletionScheduledAt) != 0)
            throw new IllegalArgumentException("Deletion scheduled at must equal to updated at");

        if (updatedAt != null && updatedAt.isBefore(createdAt))
            throw new IllegalArgumentException("Updated at must not be before created at");

        if (verified && updatedAt == null)
            throw new IllegalArgumentException("Updated at must not be null when user is verified");

        return new Account(
            id,
            email,
            encodedPassword,
            role,
            createdAt,
            updatedAt,
            deletionScheduledAt,
            verified
        );
    }

    /**
     * Marks the user as verified.
     *
     * <p>The operation is idempotent. The caller is responsible for validating
     * and consuming the corresponding email-verification challenge first.</p>
     *
     * @param clock the source of the verification time
     * @throws NullPointerException when {@code clock} is {@code null}
     * @throws UserVerificationFailedException when deletion is scheduled
     * @throws IllegalArgumentException when the verification time precedes an
     * existing aggregate timestamp
     */
    public void verify (Clock clock)
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (deletionScheduledAt != null)
            throw new UserVerificationFailedException("User is scheduled for deletion, cannot be verified");

        if (verified)
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.verified = true;
        this.updatedAt = updateTime;
    }

    /**
     * Records that deletion has been scheduled at the current business time.
     * The operation is idempotent when deletion is already scheduled.
     *
     * @param clock the source of the scheduling time
     * @throws NullPointerException when {@code clock} is {@code null}
     * @throws IllegalArgumentException when the scheduling time precedes an
     * existing aggregate timestamp
     */
    public void setDeletionSchedule (Clock clock)
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (deletionScheduledAt != null)
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.deletionScheduledAt = updateTime;
        this.updatedAt = updateTime;
    }

    /**
     * Cancels a pending deletion schedule. The operation is idempotent when no
     * deletion is scheduled.
     *
     * @param clock the source of the cancellation time
     * @throws NullPointerException when {@code clock} is {@code null}
     * @throws IllegalArgumentException when the cancellation time precedes an
     * existing aggregate timestamp
     */
    public void removeDeletionSchedule (Clock clock)
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (deletionScheduledAt == null)
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.deletionScheduledAt = null;
        this.updatedAt = updateTime;
    }

    /**
     * Installs an email address whose change challenge has already completed.
     * Consequently, replacing the email does not clear the aggregate's
     * verification state.
     *
     * @param newEmail the validated and challenge-confirmed email address
     * @param clock the source of the update time
     * @throws NullPointerException when an argument is {@code null}
     * @throws UserUpdateFailedException when deletion is scheduled
     * @throws IllegalArgumentException when the update time precedes an
     * existing aggregate timestamp
     */
    public void updateEmail (
        Email newEmail,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (newEmail == null)
            throw new NullPointerException("New email must not be null");

        if (deletionScheduledAt != null)
            throw new UserUpdateFailedException("User is scheduled for deletion, cannot be updated");

        if (email.equals(newEmail))
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.email = newEmail;
        this.updatedAt = updateTime;
    }

    /**
     * Installs a replacement password after it has been encoded and the caller
     * has completed the required password-change or recovery challenge.
     *
     * @param newEncodedPassword the externally encoded replacement password
     * @param clock the source of the update time
     * @throws NullPointerException when an argument is {@code null}
     * @throws UserUpdateFailedException when deletion is scheduled
     * @throws IllegalArgumentException when the update time precedes an
     * existing aggregate timestamp
     */
    public void updateEncodedPassword (
        EncodedPassword newEncodedPassword,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (newEncodedPassword == null)
            throw new NullPointerException("New encoded password must not be null");

        if (deletionScheduledAt != null)
            throw new UserUpdateFailedException("User is scheduled for deletion, cannot be updated");

        if (encodedPassword.equals(newEncodedPassword))
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.encodedPassword = newEncodedPassword;
        this.updatedAt = updateTime;
    }

    /**
     * Replaces the authorization role after an administrative use case has
     * authorized the change. This method is not a self-service role elevation
     * boundary.
     *
     * @param newRole the replacement authorization role
     * @param clock the source of the update time
     * @throws NullPointerException when an argument is {@code null}
     * @throws UserUpdateFailedException when deletion is scheduled
     * @throws IllegalArgumentException when the update time precedes an
     * existing aggregate timestamp
     */
    public void updateRole (
        Role newRole,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (newRole == null)
            throw new NullPointerException("New role must not be null");

        if (deletionScheduledAt != null)
            throw new UserUpdateFailedException("User is scheduled for deletion, cannot be updated");

        if (role.equals(newRole))
            return;

        Instant updateTime = clock.instant();
        checkUpdateTime(updateTime);

        this.role = newRole;
        this.updatedAt = updateTime;
    }

    private void checkUpdateTime (Instant updateTime)
    {
        if (updateTime == null)
            throw new NullPointerException("Update time must not be null");

        if (updateTime.isBefore(createdAt))
            throw new IllegalArgumentException("Update time must not be before created at");

        if (updatedAt != null && updateTime.isBefore(updatedAt))
            throw new IllegalArgumentException("Update time must not be before updated at");
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof Account that))
            return false;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode ()
    {
        return id.hashCode();
    }
}
