package online.springshop.api.modules.auth.domain.user.exception;

import lombok.Getter;

/**
 * Raised when an untrusted password value violates a password domain rule.
 * The associated {@link PasswordError} provides a stable, machine-readable reason
 * while the exception message provides a human-readable description.
 */
public final class InvalidPasswordException extends RuntimeException
{
    /** The specific password rule that was violated. */
    @Getter
    private final PasswordError error;

    /**
     * Creates an exception for a password validation failure.
     *
     * @param message a human-readable description of the failure
     * @param error the stable error identifying the violated rule
     */
    public InvalidPasswordException (String message, PasswordError error)
    {
        super(message);

        this.error = error;
    }
}
