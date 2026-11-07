package online.springshop.api.modules.auth.domain.useraction.exception;

import lombok.Getter;

/**
 * Raised when an untrusted confirmation-code value violates the confirmation
 * code policy.
 */
public final class InvalidConfirmationCodeException extends RuntimeException
{
    @Getter
    private final ConfirmationCodeError error;

    /**
     * @param message a human-readable description of the validation failure
     * @param error the stable error identifying the violated rule
     */
    public InvalidConfirmationCodeException (String message, ConfirmationCodeError error)
    {
        super(message);

        this.error = error;
    }
}
