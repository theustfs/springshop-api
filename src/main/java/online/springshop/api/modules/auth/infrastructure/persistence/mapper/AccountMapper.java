package online.springshop.api.modules.auth.infrastructure.persistence.mapper;

import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public interface AccountMapper
{
    default Account toDomain (AccountEntity entity)
    {
        if (entity == null)
            return null;

        return Account.restore(
            entity.getId(),
            Email.restore(entity.getEmail()),
            EncodedPassword.from(entity.getEncodedPassword()),
            entity.getRole(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletionScheduledAt(),
            entity.isVerified()
        );
    }

    default AccountEntity toEntity (Account account)
    {
        if (account == null)
            return null;

        return new AccountEntity(
            account.getId(),
            account.getEmail().getValue(),
            account.getEncodedPassword().getValue(),
            account.getRole(),
            account.getCreatedAt(),
            account.getUpdatedAt(),
            account.getDeletionScheduledAt(),
            account.isVerified()
        );
    }
}
