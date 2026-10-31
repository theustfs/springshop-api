package online.springshop.api.modules.auth.domain.user.valueobject;

import online.springshop.api.modules.auth.domain.user.exception.InvalidPasswordException;
import online.springshop.api.modules.auth.domain.user.exception.PasswordError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static online.springshop.api.fixtures.PasswordFixtures.getMinimumLengthValue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password tests")
class PasswordTest
{
    private static final String FIXTURES = "online.springshop.api.fixtures.PasswordFixtures";

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests
    {
        @Test
        @DisplayName("Should reject null value")
        void shouldRejectNullValue ()
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(null)
            );

            assertEquals(PasswordError.IS_NULL, exception.getError());
        }

        @ValueSource(strings = {
            "",
            " ",
            "   "
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject blank values")
        void shouldRejectBlankValues (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValidValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should accept valid values")
        void shouldAcceptValidValues (String value)
        {
            Password password = assertDoesNotThrow(
                () -> Password.of(value)
            );

            assertNotNull(password);
            assertEquals(value, password.getValue());
        }

        @MethodSource(FIXTURES + "#getTooShortValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too short values")
        void shouldRejectTooShortValues (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.IS_TOO_SHORT, exception.getError());
        }

        @MethodSource(FIXTURES + "#getTooLongValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too long values")
        void shouldRejectTooLongValues (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.IS_TOO_LONG, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithWhitespace")
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject values with whitespace")
        void shouldRejectValuesWithWhitespace (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.HAS_WHITESPACE, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithLongRepeatingCharacterSequence")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject long sequences of repeating characters")
        void shouldRejectLongSequencesOfRepeatingCharacters (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.HAS_LONG_SEQUENCE_OF_REPEATING_CHARACTER, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithLongAlphabeticSequence")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject long alphabetic sequences")
        void shouldRejectLongAlphabeticSequences (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.HAS_LONG_ALPHABETIC_SEQUENCE, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithLongNumericSequence")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject long numeric sequences")
        void shouldRejectLongNumericSequences (String value)
        {
            InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> Password.of(value)
            );

            assertEquals(PasswordError.HAS_LONG_NUMERIC_SEQUENCE, exception.getError());
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
            String value = getMinimumLengthValue();

            Password password = Password.restore(value);

            assertNotNull(password);
            assertEquals(value, password.getValue());
        }

        @Test
        @DisplayName("Should not restore when value is null")
        void shouldNotRestoreWhenValueIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> Password.restore(null)
            );
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
            String value = "8KkjVT2Z";

            Password password1 = Password.of(value);
            Password password2 = Password.of(value);

            assertEquals(password1, password2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            Password password1 = Password.of("8KkjVT2Z");
            Password password2 = Password.of("9KkjVT2Z");

            assertNotEquals(password1, password2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "8KkjVT2Z";

            Password password1 = Password.of(value);
            Password password2 = Password.of(value);

            assertEquals(password1.hashCode(), password2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            Password password1 = Password.of("8KkjVT2Z");
            Password password2 = Password.of("9KkjVT2Z");

            assertNotEquals(password1.hashCode(), password2.hashCode());
        }
    }
}
