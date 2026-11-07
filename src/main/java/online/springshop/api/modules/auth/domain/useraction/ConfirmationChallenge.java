package online.springshop.api.modules.auth.domain.useraction;

import lombok.Getter;
import online.springshop.api.modules.auth.domain.useraction.abstraction.ConfirmationCodeMatcher;
import online.springshop.api.modules.auth.domain.useraction.exception.ConfirmationChallengeCompletionError;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationChallengePolicy;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;
import online.springshop.api.shared.result.Result;
import online.springshop.api.shared.result.ResultError;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * A single confirmation step within a {@link UserAction}.
 *
 * <p>The challenge stores only an encoded confirmation code. A newly generated
 * challenge is inactive, incomplete, and has no consumed attempts. Activation
 * assigns its expiration time once; later activation calls are idempotent.</p>
 *
 * <p>Completion first rejects expired challenges and challenges that have
 * reached the attempt limit. Every code comparison consumes one attempt,
 * including a successful comparison. Completing an already completed challenge
 * is idempotent and does not consume another attempt.</p>
 *
 * <p>Persisted challenges have identity based on their database identifier.
 * Newly generated challenges have no identifier and are equal only to
 * themselves until restored with persisted identity.</p>
 */
@Getter
public final class ConfirmationChallenge
{
    private final Long id;
    private final EncodedConfirmationCode encodedConfirmationCode;
    private int attempts;
    private boolean completed;
    private Instant expiresAt;

    private ConfirmationChallenge (
        Long id,
        EncodedConfirmationCode encodedConfirmationCode,
        int attempts,
        boolean completed,
        Instant expiresAt
    )
    {
        this.id = id;
        this.encodedConfirmationCode = Objects.requireNonNull(encodedConfirmationCode, "Encoded confirmation code must not be null");
        this.attempts = attempts;
        this.completed = completed;
        this.expiresAt = expiresAt;
    }

    /**
     * Creates a new challenge from an encoded code generated inside the API.
     * New challenges begin inactive, not completed, and with no consumed attempts;
     * the owning aggregate activates them in sequence.
     *
     * @param encodedConfirmationCode the API-generated encoded code
     * @return a new inactive confirmation challenge
     * @throws NullPointerException when the encoded confirmation code is {@code null}
     */
    public static ConfirmationChallenge from (EncodedConfirmationCode encodedConfirmationCode)
    {
        int attempts = 0;
        boolean completed = false;

        return new ConfirmationChallenge(null, encodedConfirmationCode, attempts, completed, null);
    }

    /**
     * Restores a challenge from trusted state loaded from the database.
     * Persistence adapters must not use this factory for request data or other
     * untrusted values.
     *
     * <p>A restored identifier is required. Attempt counts must be between zero
     * and the configured maximum, inclusive, and a completed challenge must have
     * consumed at least one attempt.</p>
     *
     * @param id the persisted challenge identifier
     * @param encodedConfirmationCode the persisted encoded code
     * @param attempts the persisted number of consumed attempts
     * @param completed whether the persisted challenge is completed
     * @param expiresAt the persisted expiration time
     * @return the restored confirmation challenge
     * @throws NullPointerException when the identifier or encoded code is {@code null}
     * @throws IllegalArgumentException when the attempt count or completed state
     * violates a restoration invariant
     */
    public static ConfirmationChallenge restore (
        Long id,
        EncodedConfirmationCode encodedConfirmationCode,
        int attempts,
        boolean completed,
        Instant expiresAt
    )
    {
        if (id == null)
            throw new NullPointerException("Confirmation challenge id must not be null when restoring confirmation challenge");

        if (attempts < 0)
            throw new IllegalArgumentException("Attempt count must not be negative");

        if (attempts > ConfirmationChallengePolicy.MAX_ATTEMPTS)
            throw new IllegalArgumentException("Attempt count must not be greater than maximum number of attempts");

        if (completed && attempts == 0)
            throw new IllegalArgumentException("Confirmation challenge must have at least one attempt when completed");

        return new ConfirmationChallenge(id, encodedConfirmationCode, attempts, completed, expiresAt);
    }

    /**
     * Activates this challenge using the configured challenge duration. The
     * operation is idempotent when an expiration time is already present.
     *
     * @param clock the source of the activation time
     * @throws NullPointerException when {@code clock} is {@code null}
     */
    void activate (Clock clock)
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (expiresAt != null)
            return;

        expiresAt = clock.instant().plus(ConfirmationChallengePolicy.DURATION);
    }

    /**
     * Reports whether this challenge has been activated. This method only checks
     * whether an expiration timestamp is present; it does not determine whether
     * the challenge has expired or has already been completed.
     *
     * @return {@code true} when an expiration timestamp is present
     */
    public boolean isActive ()
    {
        return expiresAt != null;
    }

    /**
     * Attempts to complete this challenge with a submitted code.
     *
     * <p>Completed challenges return success without changing their state. For an
     * incomplete challenge, expiration is checked before the attempt limit. A
     * comparison is performed only when both checks pass, and that comparison
     * consumes one attempt regardless of its result.</p>
     *
     * @param confirmationCode the submitted raw code
     * @param confirmationCodeMatcher the domain service used to compare raw and encoded codes
     * @param clock the source of the confirmation time
     * @return success when completed, otherwise a structured challenge error
     * @throws NullPointerException when an argument is {@code null}
     */
    Result<Void, ConfirmationChallengeCompletionError> complete (
        ConfirmationCode confirmationCode,
        ConfirmationCodeMatcher confirmationCodeMatcher,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (confirmationCode == null)
            throw new NullPointerException("Confirmation code must not be null");

        if (confirmationCodeMatcher == null)
            throw new NullPointerException("Confirmation code matcher must not be null");

        if (completed)
            return Result.success();

        if (!clock.instant().isBefore(expiresAt))
            return Result.failure(
                new ResultError<>("Confirmation challenge has expired", ConfirmationChallengeCompletionError.EXPIRED)
            );

        if (attempts >= ConfirmationChallengePolicy.MAX_ATTEMPTS)
            return Result.failure(
                new ResultError<>(
                    "Confirmation challenge has reached the maximum number of attempts",
                    ConfirmationChallengeCompletionError.REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS
                )
            );

        boolean matches = confirmationCodeMatcher.matches(confirmationCode, encodedConfirmationCode);
        attempts++;

        if (!matches)
            return Result.failure(
                new ResultError<>("Confirmation code does not match", ConfirmationChallengeCompletionError.CONFIRMATION_CODE_DOES_NOT_MATCH)
            );

        completed = true;

        return Result.success();
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof ConfirmationChallenge that))
            return false;

        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode ()
    {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
