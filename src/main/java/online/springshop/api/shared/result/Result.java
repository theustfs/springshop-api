package online.springshop.api.shared.result;

import java.util.Objects;

/**
 * Represents the outcome of an action as either a successful value or a structured error.
 * A successful result contains a non-null {@code value} and no error, except when
 * its value type is {@link Void}. A failed result contains a non-null
 * {@link ResultError} and no value.
 *
 * @param <T> the type of value produced by a successful action
 * @param <V> the enum type used for error codes
 */
public final class Result<T, V extends Enum<V>>
{
    private final T value;
    private final ResultError<V> error;

    private Result (T value, ResultError<V> error)
    {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful result containing the action value.
     *
     * @param value the non-null value produced by the action
     * @param <T> the type of the successful value
     * @param <V> the enum type used for error codes
     * @return a successful result
     * @throws NullPointerException when {@code value} is {@code null}; use {@link #success()}
     * when the action does not produce a value
     */
    public static <T, V extends Enum<V>> Result<T, V> success (T value)
    {
        return new Result<>(Objects.requireNonNull(value, "Result value must not be null on success"), null);
    }

    /**
     * Creates a successful result for an action that does not produce a value.
     *
     * @param <V> the enum type used for error codes
     * @return a successful result whose value type is {@link Void}
     */
    public static <V extends Enum<V>> Result<Void, V> success ()
    {
        return new Result<>(null, null);
    }

    /**
     * Creates a failed result containing the action error.
     *
     * @param error the non-null error produced by the action
     * @param <T> the type of value the action would produce on success
     * @param <V> the enum type used for error codes
     * @return a failed result
     * @throws NullPointerException when {@code error} is {@code null}
     */
    public static <T, V extends Enum<V>> Result<T, V> failure (ResultError<V> error)
    {
        if (error == null)
            throw new NullPointerException("Result error must not be null on failure");

        return new Result<>(null, error);
    }

    /**
     * @return {@code true} when this result contains a successful value
     */
    public boolean isSuccess ()
    {
        return error == null;
    }

    /**
     * @return {@code true} when this result contains an error
     */
    public boolean isFailure ()
    {
        return !isSuccess();
    }

    /**
     * @return the successful action value, or {@code null} when this result is a failure
     * or its value type is {@link Void}
     */
    public T value ()
    {
        return value;
    }

    /**
     * @return the action error, or {@code null} when this result is successful
     */
    public ResultError<V> error ()
    {
        return error;
    }
}
