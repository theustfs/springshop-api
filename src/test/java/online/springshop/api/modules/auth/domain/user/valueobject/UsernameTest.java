package online.springshop.api.modules.auth.domain.user.valueobject;

import online.springshop.api.modules.auth.domain.user.exception.InvalidUsernameException;
import online.springshop.api.modules.auth.domain.user.exception.UsernameError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Username tests")
class UsernameTest
{
    private static final String FIXTURES = "online.springshop.api.fixtures.UsernameFixtures";

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests
    {
        @Test
        @DisplayName("Should reject null values")
        void shouldRejectNullValues ()
        {
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class,
                () -> Username.of(null)
            );

            assertEquals(UsernameError.IS_NULL, exception.getError());
        }

        @ValueSource(strings = {
            "",
            " ",
            "  "
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject blank values")
        void shouldRejectBlankValues (String value)
        {
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class,
                () -> Username.of(value)
            );

            assertEquals(UsernameError.IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValidValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should accept valid values")
        void shouldAcceptValidValues (String value)
        {
            Username username = assertDoesNotThrow(
                () -> Username.of(value)
            );

            assertNotNull(username);
            assertEquals(value, username.getValue());
        }

        @MethodSource(FIXTURES + "#getTooShortValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too short values")
        void shouldRejectTooShortValues (String value)
        {
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class,
                () -> Username.of(value)
            );

            assertEquals(UsernameError.IS_TOO_SHORT, exception.getError());
        }

        @MethodSource(FIXTURES + "#getTooLongValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too long values")
        void shouldRejectTooLongValues (String value)
        {
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class,
                () -> Username.of(value)
            );

            assertEquals(UsernameError.IS_TOO_LONG, exception.getError());
        }

        @ValueSource(strings = {
            "username!",
            "username@",
            "username#",
            "username$",
            "username&",
            "username*",
            "username(",
            "username)",
            "username-",
            "username_",
            "username=",
            "username+",
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject values with invalid characters")
        void shouldRejectValuesWithInvalidCharacters (String value)
        {
            InvalidUsernameException exception = assertThrows(
                InvalidUsernameException.class,
                () -> Username.of(value)
            );

            assertEquals(UsernameError.HAS_INVALID_CHARACTERS, exception.getError());
        }
    }

    @Nested
    @DisplayName("Restoration tests")
    class RestorationTests
    {
        @Test
        @DisplayName("Should restore value")
        void shouldRestoreValue ()
        {
            String value = "username";

            Username username = Username.restore(value);

            assertNotNull(username);
            assertEquals(value, username.getValue());
        }

        @Test
        @DisplayName("Should not restore when value is null")
        void shouldNotRestoreWhenValueIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> Username.restore(null)
            );
        }
    }

    @Nested
    @DisplayName("Normalization tests")
    class NormalizationTests
    {
        @MethodSource(FIXTURES + "#getRawNormalizedValuePair")
        @ParameterizedTest(name = "{argumentSetName}: ''{0}'' -> {1}")
        @DisplayName("Should normalize and store value")
        void shouldNormalizeAndStoreValue (String raw, String normalized)
        {
            Username username = Username.of(raw);

            assertNotNull(username);
            assertEquals(normalized, username.getValue());
        }
    }

    @Nested
    @DisplayName("Equals and hash code tests")
    class EqualsAndHashCodeTests
    {
        @Test
        @DisplayName("Should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual ()
        {
            String value = "username";

            Username username1 = Username.of(value);
            Username username2 = Username.of(value);

            assertEquals(username1, username2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "user name";
            String value2 = "username";

            Username username1 = Username.of(value1);
            Username username2 = Username.of(value2);

            assertNotEquals(username1, username2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "username";

            Username username1 = Username.of(value);
            Username username2 = Username.of(value);

            assertEquals(username1.hashCode(), username2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "user name";
            String value2 = "username";

            Username username1 = Username.of(value1);
            Username username2 = Username.of(value2);

            assertNotEquals(username1.hashCode(), username2.hashCode());
        }
    }
}
