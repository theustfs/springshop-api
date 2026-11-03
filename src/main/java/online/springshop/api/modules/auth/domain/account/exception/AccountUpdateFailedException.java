package online.springshop.api.modules.auth.domain.account.exception;

public final class AccountUpdateFailedException extends RuntimeException
{
    public AccountUpdateFailedException(String message)
    {
        super(message);
    }
}
