package online.springshop.api.modules.auth.domain.user.exception;

import lombok.Getter;

/**
 * Raised when an untrusted email value violates an email domain rule.
 * The associated {@link EmailError} provides a stable, machine-readable reason
 * while the exception message provides a human-readable description.
 */
public final class InvalidEmailException extends RuntimeException
{
    /** The specific email rule that was violated. */
    @Getter
    private final EmailError error;

    /**
     * Creates an exception for an email validation failure.
     *
     * @param message a human-readable description of the failure
     * @param error the stable error identifying the violated rule
     */
    public InvalidEmailException (String message, EmailError error)
    {
        super(message);

        this.error = error;
    }
}
