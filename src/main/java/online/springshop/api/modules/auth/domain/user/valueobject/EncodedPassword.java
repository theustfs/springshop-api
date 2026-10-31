package online.springshop.api.modules.auth.domain.user.valueobject;

import lombok.Getter;

/**
 * Wraps a password value that has already been encoded by an external password
 * encoder. This value object does not encode or hash passwords itself.
 */
public final class EncodedPassword
{
    @Getter
    private final String value;

    private EncodedPassword (String value)
    {
        this.value = value;
    }

    /**
     * Wraps an already encoded password value.
     * The caller is responsible for encoding the raw password before invoking
     * this factory; this method only verifies that the supplied value is present.
     *
     * @param value the password value produced by a password encoder
     * @return a wrapper around the encoded password
     * @throws NullPointerException when the value is {@code null}
     * @throws IllegalArgumentException when the value is blank
     */
    public static EncodedPassword from (String value)
    {
        if (value == null)
            throw new NullPointerException("Encoded password value must not be null");

        if (value.isBlank())
            throw new IllegalArgumentException("Encoded password value must not be blank");

        return new EncodedPassword(value);
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof EncodedPassword that))
            return false;

        return value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
