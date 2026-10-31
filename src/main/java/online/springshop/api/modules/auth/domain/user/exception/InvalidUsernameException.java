package online.springshop.api.modules.auth.domain.user.exception;

import lombok.Getter;

/**
 * Raised when an untrusted username value violates a username domain rule.
 * The associated {@link UsernameError} provides a stable, machine-readable reason
 * while the exception message provides a human-readable description.
 */
public final class InvalidUsernameException extends RuntimeException
{
    /** The specific username rule that was violated. */
    @Getter
    private final UsernameError error;

    /**
     * Creates an exception for a username validation failure.
     *
     * @param message a human-readable description of the failure
     * @param error the stable error identifying the violated rule
     */
    public InvalidUsernameException (String message, UsernameError error)
    {
        super(message);

        this.error = error;
    }
}
