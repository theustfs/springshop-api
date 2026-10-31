package online.springshop.api.modules.auth.domain.user.exception;

/**
 * Identifies the username rule violated while creating a username from an untrusted value.
 */
public enum UsernameError
{
    /** The supplied username value is {@code null}. */
    IS_NULL,

    /** The supplied username value is empty or contains only whitespace. */
    IS_BLANK,

    /** The username is shorter than the configured minimum length. */
    IS_TOO_SHORT,

    /** The username exceeds the configured maximum length. */
    IS_TOO_LONG,

    /** The username contains a character not permitted by the username rules. */
    HAS_INVALID_CHARACTERS
}
