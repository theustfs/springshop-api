package online.springshop.api.modules.auth.domain.useraction.valueobject;

/**
 * Sensitive user operations that may require confirmation challenges before
 * their application workflow is allowed to complete.
 */
public enum ActionType
{
    ACCOUNT_REGISTRATION,

    ACCOUNT_LOGIN,

    ACCOUNT_EMAIL_UPDATE,
    ACCOUNT_EMAIL_RESET,

    ACCOUNT_PASSWORD_UPDATE,
    ACCOUNT_PASSWORD_RESET,

    ACCOUNT_DELETION
}
