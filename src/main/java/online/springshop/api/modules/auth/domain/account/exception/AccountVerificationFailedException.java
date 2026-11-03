package online.springshop.api.modules.auth.domain.account.exception;

public final class AccountVerificationFailedException extends RuntimeException
{
    public AccountVerificationFailedException(String message)
    {
        super(message);
    }
}
