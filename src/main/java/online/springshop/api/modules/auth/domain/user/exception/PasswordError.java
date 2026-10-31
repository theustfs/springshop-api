package online.springshop.api.modules.auth.domain.user.exception;

/**
 * Identifies the password rule violated while creating a password from an untrusted value.
 */
public enum PasswordError
{
    /** The supplied password value is {@code null}. */
    IS_NULL,

    /** The supplied password value is empty or contains only whitespace. */
    IS_BLANK,

    /** The password is shorter than the configured minimum length. */
    IS_TOO_SHORT,

    /** The password exceeds the configured maximum length. */
    IS_TOO_LONG,

    /** The password contains whitespace. */
    HAS_WHITESPACE,

    /** The password contains too many consecutive occurrences of one character. */
    HAS_LONG_SEQUENCE_OF_REPEATING_CHARACTER,

    /** The password contains an alphabetical sequence longer than permitted. */
    HAS_LONG_ALPHABETIC_SEQUENCE,

    /** The password contains a numeric sequence longer than permitted. */
    HAS_LONG_NUMERIC_SEQUENCE,
}
