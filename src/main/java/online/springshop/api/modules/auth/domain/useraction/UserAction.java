package online.springshop.api.modules.auth.domain.useraction;

import com.fasterxml.uuid.Generators;
import lombok.Getter;
import online.springshop.api.modules.auth.domain.useraction.abstraction.ConfirmationCodeMatcher;
import online.springshop.api.modules.auth.domain.useraction.exception.ConfirmationChallengeCompletionError;
import online.springshop.api.modules.auth.domain.useraction.exception.UserActionConfirmationError;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ActionType;
import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.shared.result.Result;
import online.springshop.api.shared.result.ResultError;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root for a user action that is authorized by completing an ordered
 * sequence of confirmation challenges.
 *
 * <p>The aggregate owns the challenge order and permits only the first
 * incomplete challenge to progress. Completing that challenge activates the
 * next incomplete challenge. When no incomplete challenge remains, subsequent
 * completion requests are idempotent and return success.</p>
 *
 * <p>New actions require at least one inactive challenge and activate the first
 * challenge using the supplied {@link Clock}. Restored actions require every
 * incomplete challenge to contain an activation timestamp. The challenge list
 * is defensively copied so callers cannot add, remove, or reorder challenges
 * after construction.</p>
 *
 * <p>Expected completion failures are returned as a
 * {@link Result} containing a {@link UserActionConfirmationError}. The aggregate
 * maps the current challenge's lower-level completion error without exposing
 * challenge implementation details to callers.</p>
 *
 * <p>Each action has its own time-based UUID and separately records the account
 * that owns it. Aggregate equality and hash code are based exclusively on the
 * user-action identifier, allowing one account to own multiple distinct actions,
 * including repeated actions of the same type.</p>
 */
@Getter
public final class UserAction
{
    private final UUID id;
    private final UUID accountId;
    private final ActionType actionType;
    private final List<ConfirmationChallenge> challenges;

    private UserAction (
        UUID id,
        UUID accountId,
        ActionType actionType,
        List<ConfirmationChallenge> challenges
    )
    {
        this.id = Objects.requireNonNull(id, "User action id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "Account id must not be null");
        this.actionType = Objects.requireNonNull(actionType, "Action type must not be null");
        this.challenges = List.copyOf(challenges);
    }

    /**
     * Creates a new user action from values generated inside the API and
     * activates its first confirmation challenge. The action receives a new
     * time-based UUID that is independent of the owning account identifier.
     *
     * <p>The challenge collection must be non-empty and every supplied challenge
     * must be inactive. Required aggregate properties and challenge elements are
     * validated before the first challenge is activated.</p>
     *
     * @param accountId the account that owns the action
     * @param actionType the operation being authorized
     * @param challenges the ordered confirmation challenges
     * @param clock the source of the first challenge's activation time
     * @return a newly created user action with its own generated identifier
     * @throws NullPointerException when a required argument or challenge is {@code null}
     * @throws IllegalArgumentException when the challenge list is empty or already
     * contains an active challenge
     */
    public static UserAction from (
        UUID accountId,
        ActionType actionType,
        List<ConfirmationChallenge> challenges,
        Clock clock
    )
    {
        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        if (challenges.isEmpty())
            throw new IllegalArgumentException("Challenges list must not be empty");

        if (challenges.stream().anyMatch(c -> c.getExpiresAt() != null))
            throw new IllegalArgumentException("All challenges must be unactive before creation");

        UUID id = Generators.timeBasedEpochGenerator().generate();

        UserAction userAction = new UserAction(id, accountId, actionType, challenges);

        userAction.getChallenges().get(0)
            .activate(clock);

        return userAction;
    }

    /**
     * Restores a user action from trusted state loaded from the database.
     * Persistence adapters must not pass request data or other untrusted values
     * to this factory.
     *
     * <p>The persisted challenge collection must be non-empty, and every
     * incomplete challenge must already be active.</p>
     *
     * @param id the persisted identifier of this user action
     * @param accountId the persisted identifier of the account that owns the action
     * @param actionType the persisted action type
     * @param challenges the persisted ordered challenge state
     * @return the restored user action
     * @throws NullPointerException when a required value or challenge is {@code null}
     * @throws IllegalArgumentException when the challenge list is empty or an
     * incomplete challenge is inactive
     */
    public static UserAction restore (
        UUID id,
        UUID accountId,
        ActionType actionType,
        List<ConfirmationChallenge> challenges
    )
    {
        if (challenges.isEmpty())
            throw new IllegalArgumentException("Challenges list must not be empty");

        challenges.stream()
            .filter(c -> !c.isCompleted() && !c.isActive())
            .findFirst()
            .ifPresent(c -> {
                throw new IllegalArgumentException("Current challenge must be active when restoring a user action");
            });

        return new UserAction(id, accountId, actionType, challenges);
    }

    /**
     * Attempts to complete the current challenge and activates the next challenge
     * after a successful completion.
     *
     * <p>The current challenge is the first incomplete challenge in the configured
     * order. Expiration, attempt-limit, and code-mismatch failures are returned as
     * structured errors. When every challenge is already completed, this method
     * returns success without changing aggregate state.</p>
     *
     * @param confirmationCode the submitted confirmation code
     * @param confirmationCodeMatcher the domain service used to compare raw and encoded codes
     * @param clock the source of business time
     * @return success when the current challenge was completed, otherwise a
     * structured confirmation error
     * @throws NullPointerException when an argument is {@code null}
     */
    public Result<Void, UserActionConfirmationError> completeCurrentChallenge (
        ConfirmationCode confirmationCode,
        ConfirmationCodeMatcher confirmationCodeMatcher,
        Clock clock
    )
    {
        if (confirmationCode == null)
            throw new NullPointerException("Confirmation code must not be null");

        if (confirmationCodeMatcher == null)
            throw new NullPointerException("Confirmation code matcher must not be null");

        if (clock == null)
            throw new NullPointerException("Clock must not be null");

        Optional<ConfirmationChallenge> currentChallenge = getCurrentChallenge();

        if (currentChallenge.isEmpty())
            return Result.success();

        ConfirmationChallenge challenge = currentChallenge.get();

        Result<Void, ConfirmationChallengeCompletionError> challengeResult = challenge.complete(confirmationCode, confirmationCodeMatcher, clock);

        if (challengeResult.isFailure())
            return getMappedResultFailure(challengeResult);

        getCurrentChallenge().ifPresent(
            nextChallenge -> nextChallenge.activate(clock)
        );

        return Result.success();
    }

    /**
     * Reports whether the action has satisfied every required confirmation step.
     *
     * @return {@code true} when every challenge in this action is completed
     */
    public boolean areAllChallengesCompleted ()
    {
        return challenges.stream()
            .allMatch(ConfirmationChallenge::isCompleted);
    }

    private Result<Void, UserActionConfirmationError> getMappedResultFailure (Result<Void, ConfirmationChallengeCompletionError> challengeResult)
    {
        UserActionConfirmationError mappedError = switch (challengeResult.error().code())
        {
            case CONFIRMATION_CODE_DOES_NOT_MATCH ->
                UserActionConfirmationError.CONFIRMATION_CODE_DOES_NOT_MATCH;

            case REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS ->
                UserActionConfirmationError.CONFIRMATION_CHALLENGE_REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS;

            case EXPIRED ->
                UserActionConfirmationError.CONFIRMATION_CHALLENGE_EXPIRED;
        };

        return Result.failure(
            new ResultError<>(challengeResult.error().message(), mappedError)
        );
    }

    private Optional<ConfirmationChallenge> getCurrentChallenge ()
    {
        return challenges.stream()
            .filter(challenge -> !challenge.isCompleted())
            .findFirst();
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof UserAction that))
            return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode ()
    {
        return id.hashCode();
    }
}
