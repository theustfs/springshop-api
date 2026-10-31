package online.springshop.api.modules.auth.domain.user.valueobject;

import lombok.Getter;
import online.springshop.api.modules.auth.domain.user.exception.InvalidUsernameException;
import online.springshop.api.modules.auth.domain.user.exception.UsernameError;
import online.springshop.api.modules.auth.domain.user.policy.UsernamePolicy;

import java.util.Locale;
import java.util.Objects;

public final class Username
{
    @Getter
    private final String value;

    private Username (String value)
    {
        this.value = Objects.requireNonNull(value, "Username value must not be null");
    }

    /**
     * Creates a username from an untrusted value, such as user input.
     * The value is normalized and validated before the username is created.
     *
     * @param value the untrusted username value
     * @return a normalized and validated username
     * @throws InvalidUsernameException when the value violates the username policy
     */
    public static Username of (String value)
    {
        if (value == null)
            throw new InvalidUsernameException(
                "Username value must not be null",
                UsernameError.IS_NULL
            );

        if (value.isBlank())
            throw new InvalidUsernameException(
                "Username value must not be blank",
                UsernameError.IS_BLANK
            );

        String normalized = normalize(value);

        validate(normalized);

        return new Username(normalized);
    }

    /**
     * Restores a username from a trusted source, such as the database.
     * This factory does not normalize or validate the value and must not be used
     * with user input or data from another untrusted source.
     *
     * @param value the trusted, previously validated username value
     * @return the restored username
     * @throws NullPointerException when the value is {@code null}
     */
    public static Username restore (String value)
    {
        return new Username(value);
    }

    private static String normalize (String value)
    {
        return value.toLowerCase(Locale.ROOT)
            .trim()
            .replaceAll("\\s+", " ");
    }

    private static void validate (String value)
    {
        validateLength(value);

        char[] chars = value.toCharArray();

        validateCharacters(chars);
    }

    private static void validateLength (String value)
    {
        if (value.length() < UsernamePolicy.MIN_LENGTH)
            throw new InvalidUsernameException(
                "Username must have at least %s characters".formatted(UsernamePolicy.MIN_LENGTH),
                UsernameError.IS_TOO_SHORT
            );

        if (value.length() > UsernamePolicy.MAX_LENGTH)
            throw new InvalidUsernameException(
                "Username must have at most %s characters".formatted(UsernamePolicy.MAX_LENGTH),
                UsernameError.IS_TOO_LONG
            );
    }

    private static void validateCharacters (char[] characters)
    {
        for (char character : characters)
            if (!Character.isAlphabetic(character) && character != ' ')
                throw new InvalidUsernameException(
                    "Username must contain only letters and spaces",
                    UsernameError.HAS_INVALID_CHARACTERS
                );
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof Username that))
            return false;

        return value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
