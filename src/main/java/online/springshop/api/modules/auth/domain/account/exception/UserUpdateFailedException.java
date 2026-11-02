package online.springshop.api.modules.auth.domain.account.exception;

public final class UserUpdateFailedException extends RuntimeException
{
    public UserUpdateFailedException(String message)
    {
        super(message);
    }
}
