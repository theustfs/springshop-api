package online.springshop.api.modules.auth.domain.useraction.exception;

import lombok.Getter;

/**
 * Raised when a user-action confirmation workflow cannot complete its current
 * challenge.
 */
public final class UserActionConfirmationFailedException extends RuntimeException
{
    @Getter
    private final UserActionConfirmationError error;

    /**
     * @param message a human-readable description of the completion failure
     * @param error the stable error identifying the failure reason
     */
    public UserActionConfirmationFailedException (String message, UserActionConfirmationError error)
    {
        super(message);

        this.error = error;
    }
}
