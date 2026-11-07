package online.springshop.api.modules.auth.domain.useraction.exception;

/** Stable reasons why a user action's current challenge could not be completed. */
public enum UserActionConfirmationError
{
    CONFIRMATION_CODE_DOES_NOT_MATCH,
    CONFIRMATION_CHALLENGE_EXPIRED,
    CONFIRMATION_CHALLENGE_REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS
}
