package online.springshop.api.modules.auth.domain.useraction.exception;

/** Stable reasons why a confirmation challenge could not be completed. */
public enum ConfirmationChallengeCompletionError
{
    CONFIRMATION_CODE_DOES_NOT_MATCH,
    EXPIRED,
    REACHED_MAXIMUM_NUMBER_OF_ATTEMPTS
}
