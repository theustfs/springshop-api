package online.springshop.api.fixtures;

import com.fasterxml.uuid.Generators;
import online.springshop.api.modules.auth.domain.account.Account;
import online.springshop.api.modules.auth.domain.account.Role;
import online.springshop.api.modules.auth.domain.account.valueobject.Email;
import online.springshop.api.modules.auth.domain.account.valueobject.EncodedPassword;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Stream;


public class AccountFixtures
{
    private static final Email OLD_EMAIL = Email.restore("test@example.com");
    private static final EncodedPassword OLD_ENCODED_PASSWORD = EncodedPassword.from("encoded_password");
    private static final Role OLD_ROLE = Role.USER;

    private static final Email NEW_EMAIL = Email.restore("new_test@example.com");
    private static final EncodedPassword NEW_ENCODED_PASSWORD = EncodedPassword.from("new_encoded_password");
    private static final Role NEW_ROLE = Role.ADMIN;

    @FunctionalInterface
    public interface AccountUpdater<T>
    {
        void update (Account account, T value, Clock clock);
    }

    public record UpdateCase<T> (
        AccountUpdater<T> updater,
        Function<Account, T> field,
        T oldValue,
        T newValue
    )
    {
        public void updateToNewValue (Account account, Clock clock)
        {
            updater.update(account, newValue, clock);
        }

        public void updateToOldValue (Account account, Clock clock)
        {
            updater.update(account, oldValue, clock);
        }

        public void updateToNull (Account account, Clock clock)
        {
            updater.update(account, null, clock);
        }

        public T readField (Account account)
        {
            return field.apply(account);
        }
    }

    public static Account createAccount (
        Instant createdAt,
        Instant updatedAt,
        Instant deletionScheduledAt,
        boolean verified
    )
    {
        return Account.restore(
            Generators.timeBasedEpochGenerator().generate(),
            OLD_EMAIL,
            OLD_ENCODED_PASSWORD,
            OLD_ROLE,
            createdAt,
            updatedAt,
            deletionScheduledAt,
            verified
        );
    }

    static Stream<Arguments> getUpdateCases ()
    {
        return Stream.of(
            Arguments.argumentSet(
                "email",
                new UpdateCase<>(
                    Account::updateEmail,
                    Account::getEmail,
                    OLD_EMAIL, NEW_EMAIL
                )
            ),
            Arguments.argumentSet(
                "encoded password",
                new UpdateCase<>(
                    Account::updateEncodedPassword,
                    Account::getEncodedPassword,
                    OLD_ENCODED_PASSWORD,
                    NEW_ENCODED_PASSWORD
                )
            ),
            Arguments.argumentSet(
                "role",
                new UpdateCase<>(
                    Account::updateRole,
                    Account::getRole,
                    OLD_ROLE,
                    NEW_ROLE
                )
            )
        );
    }
}
