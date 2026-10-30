package online.springshop.api.shared.result;

/**
 * Describes an action failure with a human-readable message and a stable enum code.
 *
 * @param message the non-null human-readable description of the failure
 * @param code the non-null enum value that identifies the failure
 * @param <V> the enum type used for error codes
 */
public record ResultError<V extends Enum<V>> (
    String message,
    V code
)
{
    /**
     * Creates a result error after validating its required properties.
     *
     * @throws NullPointerException when {@code message} or {@code code} is {@code null}
     */
    public ResultError
    {
        if (message == null)
            throw new NullPointerException("Result error message must not be null");

        if  (code == null)
            throw new NullPointerException("Result error code must not be null");
    }
}
