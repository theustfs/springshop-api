package online.springshop.api.modules.auth.domain.user.valueobject;

import lombok.Getter;
import online.springshop.api.modules.auth.domain.user.exception.EmailError;
import online.springshop.api.modules.auth.domain.user.exception.InvalidEmailException;
import online.springshop.api.modules.auth.domain.user.policy.EmailPolicy;

import java.util.Locale;
import java.util.Objects;

public final class Email
{
    @Getter
    private final String value;

    private Email (String value)
    {
        this.value = Objects.requireNonNull(value, "Email value must not be null");
    }

    /**
     * Creates an email from an untrusted value, such as user input.
     * The value is normalized and validated before the email is created.
     *
     * @param value the untrusted email value
     * @return a normalized and validated email
     * @throws InvalidEmailException when the value violates the email policy
     */
    public static Email of (String value)
    {
        if (value == null)
            throw new InvalidEmailException(
                "Email value must not be null",
                EmailError.IS_NULL
            );
        if (value.isBlank())
            throw new InvalidEmailException(
                "Email value must not be blank",
                EmailError.IS_BLANK
            );

        String normalized = normalize(value);

        validate(normalized);

        return new Email(normalized);
    }

    /**
     * Restores an email from a trusted source, such as the database.
     * This factory does not normalize or validate the value and must not be used
     * with user input or data from another untrusted source.
     *
     * @param value the trusted, previously validated email value
     * @return the restored email
     * @throws NullPointerException when the value is {@code null}
     */
    public static Email restore (String value)
    {
        return new Email(value);
    }

    private static String normalize (String value)
    {
        return value.trim()
            .toLowerCase(Locale.ROOT);
    }

    private static void validate (String value)
    {
        if (value.length() > EmailPolicy.MAX_TOTAL_LENGTH)
            throw new InvalidEmailException(
                "Email must have at most %s characters".formatted(EmailPolicy.MAX_TOTAL_LENGTH),
                EmailError.IS_TOO_LONG
            );

        if (value.chars().anyMatch(Character::isWhitespace))
            throw new InvalidEmailException(
                "Email must not contain whitespace",
                EmailError.HAS_WHITESPACE
            );

        String[] parts = value.split("@", -1);

        if (parts.length != 2)
            throw new InvalidEmailException(
                "Email must contain exactly one at sign (@)",
                EmailError.IS_MALFORMED
            );

        validateLocalPart(parts[0]);
        validateDomainPart(parts[1]);
    }

    private static void validateLocalPart (String localPart)
    {
        if (localPart.isBlank())
            throw new InvalidEmailException(
                "Email local part must not be empty",
                EmailError.LOCAL_PART_IS_BLANK
            );

        if (localPart.length() > EmailPolicy.MAX_LOCAL_PART_LENGTH)
            throw new InvalidEmailException(
                "Email local part must have at most %s characters".formatted(EmailPolicy.MAX_LOCAL_PART_LENGTH),
                EmailError.LOCAL_PART_IS_TOO_LONG
            );

        if (localPart.startsWith(".") || localPart.endsWith("."))
            throw new InvalidEmailException(
                "Email local part must not start or end with a dot",
                EmailError.LOCAL_PART_START_OR_END_WITH_DOT
            );

        if (localPart.contains(".."))
            throw new InvalidEmailException(
                "Email local part must not contain consecutive dots",
                EmailError.LOCAL_PART_HAS_CONSECUTIVE_DOTS
            );

        if (!localPart.chars().allMatch(Email::isValidLocalPartCharacter))
            throw new InvalidEmailException(
                "Email local part contains invalid characters",
                EmailError.LOCAL_PART_HAS_INVALID_CHARACTERS
            );

    }

    private static void validateDomainPart (String domainPart)
    {
        if (domainPart.isEmpty())
            throw new InvalidEmailException(
                "Email domain part must not be empty",
                EmailError.DOMAIN_PART_IS_BLANK
            );

        if (domainPart.startsWith(".") || domainPart.endsWith("."))
            throw new InvalidEmailException(
                "Email domain part must not start or end with a dot",
                EmailError.DOMAIN_PART_START_OR_END_WITH_DOT
            );

        if (domainPart.contains(".."))
            throw new InvalidEmailException(
                "Email domain part must not contain consecutive dots",
                EmailError.DOMAIN_PART_HAS_CONSECUTIVE_DOTS
            );

        String[] labels = domainPart.split("\\.", -1);

        if (labels.length < EmailPolicy.MIN_DOMAIN_LABELS)
            throw new InvalidEmailException(
                "Email domain part must include a domain and a top-level domain",
                EmailError.DOMAIN_PART_HAS_TOO_FEW_LABELS
            );

        validateDomainLabels(labels);
    }

    private static void validateDomainLabels (String[] labels)
    {
        for (String label : labels)
        {
            if (label.length() > EmailPolicy.MAX_DOMAIN_LABEL_LENGTH)
                throw new InvalidEmailException(
                    "Each email domain label must have at most %s characters".formatted(EmailPolicy.MAX_DOMAIN_LABEL_LENGTH),
                    EmailError.DOMAIN_LABEL_IS_TOO_LONG
                );

            if (label.startsWith("-") || label.endsWith("-"))
                throw new InvalidEmailException(
                    "An email domain label must not start or end with a hyphen",
                    EmailError.DOMAIN_LABEL_START_OR_END_WITH_HYPHEN
                );

            if (!label.chars().allMatch(Email::isValidDomainLabelCharacter))
                throw new InvalidEmailException(
                    "Email domain labels must contain only letters, digits, and hyphens",
                    EmailError.DOMAIN_LABEL_HAS_INVALID_CHARACTERS
                );
        }
    }

    private static boolean isValidLocalPartCharacter (int character)
    {
        return character >= 'a' && character <= 'z'
            || character >= '0' && character <= '9'
            || "!#$%&'*+-/=?^_`{|}~.".indexOf(character) >= 0;
    }

    private static boolean isValidDomainLabelCharacter (int character)
    {
        return character >= 'a' && character <= 'z'
            || character >= '0' && character <= '9'
            || character == '-';
    }

    @Override
    public boolean equals (Object object)
    {
        if (this == object)
            return true;

        if (!(object instanceof Email that))
            return false;

        return value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
