package online.springshop.api.modules.auth.domain.user.exception;

/**
 * Identifies the email rule violated while creating an email from an untrusted value.
 */
public enum EmailError
{
    /** The supplied email value is {@code null}. */
    IS_NULL,

    /** The supplied email value is empty or contains only whitespace. */
    IS_BLANK,

    /** The email does not contain exactly one at sign ({@code @}). */
    IS_MALFORMED,

    /** The complete email exceeds the configured maximum length. */
    IS_TOO_LONG,

    /** The email contains internal whitespace after normalization. */
    HAS_WHITESPACE,

    /** The part before the at sign is empty. */
    LOCAL_PART_IS_BLANK,

    /** The local part exceeds the configured maximum length. */
    LOCAL_PART_IS_TOO_LONG,

    /** The local part contains a character not permitted by the email rules. */
    LOCAL_PART_HAS_INVALID_CHARACTERS,

    /** The local part starts or ends with a dot. */
    LOCAL_PART_START_OR_END_WITH_DOT,

    /** The local part contains adjacent dots. */
    LOCAL_PART_HAS_CONSECUTIVE_DOTS,

    /** The part after the at sign is empty. */
    DOMAIN_PART_IS_BLANK,

    /** The domain does not contain the configured minimum number of labels. */
    DOMAIN_PART_HAS_TOO_FEW_LABELS,

    /** The domain starts or ends with a dot. */
    DOMAIN_PART_START_OR_END_WITH_DOT,

    /** The domain contains adjacent dots. */
    DOMAIN_PART_HAS_CONSECUTIVE_DOTS,

    /** A domain label exceeds the configured maximum length. */
    DOMAIN_LABEL_IS_TOO_LONG,

    /** A domain label contains a character other than a letter, digit, or hyphen. */
    DOMAIN_LABEL_HAS_INVALID_CHARACTERS,

    /** A domain label starts or ends with a hyphen. */
    DOMAIN_LABEL_START_OR_END_WITH_HYPHEN
}
