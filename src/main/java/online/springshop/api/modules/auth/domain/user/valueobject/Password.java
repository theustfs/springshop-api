package online.springshop.api.modules.auth.domain.user.valueobject;

import lombok.Getter;
import online.springshop.api.modules.auth.domain.user.exception.InvalidPasswordException;
import online.springshop.api.modules.auth.domain.user.exception.PasswordError;
import online.springshop.api.modules.auth.domain.user.policy.PasswordPolicy;

import java.util.Objects;

public final class Password
{
    @Getter
    private final String value;

    private Password (String value)
    {
        this.value = Objects.requireNonNull(value, "Password value must not be null");
    }

    /**
     * Creates a password from an untrusted value, such as user input.
     * The raw value is validated before the password is created.
     *
     * @param value the untrusted raw password value
     * @return a validated raw password
     * @throws InvalidPasswordException when the value violates the password policy
     */
    public static Password of (String value)
    {
        if (value == null)
            throw new InvalidPasswordException(
                "Password value must not be null",
                PasswordError.IS_NULL
            );

        if (value.isBlank())
            throw new InvalidPasswordException(
                "Password value must not be blank",
                PasswordError.IS_BLANK
            );

        validate(value);

        return new Password(value);
    }

    /**
     * Restores a raw password from a trusted source.
     * This factory does not validate the value and must not be used with user
     * input or data from another untrusted source.
     *
     * @param value the trusted, previously validated raw password value
     * @return the restored raw password
     * @throws NullPointerException when the value is {@code null}
     */
    public static Password restore (String value)
    {
        return new Password(value);
    }

    private static void validate (String value)
    {
        validateLength(value);

        char[] chars = value.toCharArray();

        validateCharacters(chars);
        validateRepeatedCharacters(chars);
        validateAlphanumericSequence(chars);
    }

    private static void validateLength (String value)
    {
        if (value.length() < PasswordPolicy.MIN_LENGTH)
            throw new InvalidPasswordException(
                "Password must have at least %s characters".formatted(PasswordPolicy.MIN_LENGTH),
                PasswordError.IS_TOO_SHORT
            );

        if (value.length() > PasswordPolicy.MAX_LENGTH)
            throw new InvalidPasswordException(
                "Password must have at most %s characters".formatted(PasswordPolicy.MAX_LENGTH),
                PasswordError.IS_TOO_LONG
            );
    }

    private static void validateCharacters (char[] characters)
    {
        for (char character : characters)
        {
            if (Character.isWhitespace(character))
                throw new InvalidPasswordException(
                    "Password must not contain whitespace",
                    PasswordError.HAS_WHITESPACE
                );
        }
    }

    private static void validateRepeatedCharacters (char[] characters)
    {
        int sequence = 1;

        for (int i = 1; i < characters.length; i++)
        {
            char current  = characters[i];
            char previous = characters[i-1];

            if (current == previous)
                sequence++;
            else
                sequence = 1;

            if (sequence > PasswordPolicy.MAX_SEQUENCE_OF_REPEATING_CHARACTER)
                throw new InvalidPasswordException(
                    "Password must not contain more than %s consecutive occurrences of the same character"
                        .formatted(PasswordPolicy.MAX_SEQUENCE_OF_REPEATING_CHARACTER),
                    PasswordError.HAS_LONG_SEQUENCE_OF_REPEATING_CHARACTER
                );
        }
    }

    private static void validateAlphanumericSequence (char[] characters)
    {
        int ascendingSequence = 1;
        int descendingSequence = 1;

        for (int i = 1; i < characters.length; i++)
        {
            char current  = characters[i];
            char previous = characters[i-1];

            int charSub = current - previous;

            switch (charSub)
            {
                case 1:
                    ascendingSequence++;
                    descendingSequence = 1;
                    break;
                case -1:
                    descendingSequence++;
                    ascendingSequence = 1;
                    break;
                default:
                    ascendingSequence = 1;
                    descendingSequence = 1;
            }

            int greaterSequence = Math.max(ascendingSequence, descendingSequence);

            if (greaterSequence > PasswordPolicy.MAX_ALPHABETIC_SEQUENCE && Character.isAlphabetic(current))
                throw new InvalidPasswordException(
                    "Password must not contain an ascending or descending alphabetic sequence longer than %s characters"
                        .formatted(PasswordPolicy.MAX_ALPHABETIC_SEQUENCE),
                    PasswordError.HAS_LONG_ALPHABETIC_SEQUENCE
                );
            else if (greaterSequence >  PasswordPolicy.MAX_NUMERIC_SEQUENCE && Character.isDigit(current))
                throw new InvalidPasswordException(
                    "Password must not contain an ascending or descending numeric sequence longer than %s digits"
                        .formatted(PasswordPolicy.MAX_NUMERIC_SEQUENCE),
                    PasswordError.HAS_LONG_NUMERIC_SEQUENCE
                );
        }
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof Password that))
            return false;

        return value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
