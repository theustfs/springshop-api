package online.springshop.api.modules.auth.domain.useraction.exception;

/** Stable validation errors for confirmation codes received from untrusted sources. */
public enum ConfirmationCodeError
{
    IS_NULL,
    IS_BLANK,

    HAS_INVALID_LENGTH,
    HAS_INVALID_CHARACTERS,
}
