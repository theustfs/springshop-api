package online.springshop.api.modules.auth.infrastructure.persistence.mapper;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.Role;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;
import online.springshop.api.modules.auth.infrastructure.persistence.entity.AccountEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account mapper tests")
class AccountMapperTest
{
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @Nested
    @DisplayName("Domain to entity mapping tests")
    class DomainToEntityMappingTests
    {
        private Account getAccountDomain ()
        {
            Instant now = Instant.now();

            return Account.restore(
                Generators.timeBasedEpochGenerator().generate(),
                Email.restore("test@example.com"),
                EncodedPassword.from("encoded_password"),
                Role.PRODUCT_MANAGER,
                now.minusSeconds(60),
                now.minusSeconds(30),
                now.minusSeconds(30),
                true
            );
        }

        @Test
        @DisplayName("Should map domain to entity")
        void shouldMapDomainToEntity ()
        {
            Account domain = getAccountDomain();

            AccountEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(domain.getId(), entity.getId());
            assertEquals(domain.getEmail().getValue(), entity.getEmail());
            assertEquals(domain.getEncodedPassword().getValue(), entity.getEncodedPassword());
            assertEquals(domain.getRole(), entity.getRole());
            assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
            assertEquals(domain.getUpdatedAt(), entity.getUpdatedAt());
            assertEquals(domain.getDeletionScheduledAt(), entity.getDeletionScheduledAt());
            assertEquals(domain.isVerified(), entity.isVerified());
        }

        @Test
        @DisplayName("Should return null when domain is null")
        void shouldReturnNullWhenDomainIsNull ()
        {
            assertNull(mapper.toEntity(null));
        }
    }

    @Nested
    @DisplayName("Entity to domain mapping tests")
    class EntityToDomainTests
    {
        private AccountEntity getAccountEntity ()
        {
            Instant now = Instant.now();

            return new AccountEntity(
                Generators.timeBasedEpochGenerator().generate(),
                "test@example.com",
                "encoded_password",
                Role.PRODUCT_MANAGER,
                now.minusSeconds(60),
                now.minusSeconds(30),
                now.minusSeconds(30),
                true
            );
        }

        @Test
        @DisplayName("Should map entity to domain")
        void shouldMapDomainToDomain ()
        {
            AccountEntity entity = getAccountEntity();

            Account domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(entity.getId(), domain.getId());
            assertEquals(entity.getEmail(), domain.getEmail().getValue());
            assertEquals(entity.getEncodedPassword(), domain.getEncodedPassword().getValue());
            assertEquals(entity.getRole(), domain.getRole());
            assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
            assertEquals(entity.getUpdatedAt(), domain.getUpdatedAt());
            assertEquals(entity.getDeletionScheduledAt(), domain.getDeletionScheduledAt());
            assertEquals(entity.isVerified(), domain.isVerified());
        }

        @Test
        @DisplayName("Should return null when entity is null")
        void shouldReturnNullWhenEntityIsNull ()
        {
            assertNull(mapper.toDomain(null));
        }
    }
}