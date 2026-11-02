package online.springshop.api.modules.auth.domain.account.exception;

public final class UserVerificationFailedException extends RuntimeException
{
    public UserVerificationFailedException (String message)
    {
        super(message);
    }
}
