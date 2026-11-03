package online.springshop.api.modules.auth.domain.account.exception;

public class AccountDeletionScheduleSetFailedException extends RuntimeException
{
    public AccountDeletionScheduleSetFailedException (String message)
    {
        super(message);
    }
}
