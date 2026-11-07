package online.springshop.api.modules.auth.domain.useraction.valueobject;

import online.springshop.api.modules.auth.domain.useraction.exception.ConfirmationCodeError;
import online.springshop.api.modules.auth.domain.useraction.exception.InvalidConfirmationCodeException;
import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationCodePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Confirmation code tests")
class ConfirmationCodeTest
{
    private static final String FIXTURES = "online.springshop.api.fixtures.ConfirmationCodeFixtures";

    @Nested
    @DisplayName("Generated value validation")
    class GeneratedValueValidation
    {
        @MethodSource(FIXTURES + "#getValidValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should accept valid value")
        void shouldAcceptValidValues (String value)
        {
            ConfirmationCode confirmationCode = assertDoesNotThrow(
                () -> ConfirmationCode.from(value)
            );

            assertNotNull(confirmationCode);
            assertEquals(value, confirmationCode.getValue());
        }

        @Test
        @DisplayName("Should reject null value")
        void shouldRejectNullValue ()
        {
            assertThrows(
                NullPointerException.class,
                () -> ConfirmationCode.from(null)
            );
        }

        @ValueSource(strings = {
            "",
            "  ",
            "   "
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject value with invalid length")
        void shouldRejectBlankValue (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationCode.from(value)
            );
        }

        @MethodSource(FIXTURES + "#getValuesWithInvalidLength")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject values with invalid length")
        void shouldRejectValuesWithInvalidLength (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationCode.from(value)
            );
        }

        @MethodSource(FIXTURES + "#getValuesWithInvalidCharacters")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject values with invalid character")
        void shouldRejectValuesWithInvalidCharacters (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> ConfirmationCode.from(value)
            );
        }
    }

    @Nested
    @DisplayName("Submitted value validation")
    class SubmittedValueValidation
    {
        @MethodSource(FIXTURES + "#getValidValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should accept valid value")
        void shouldAcceptValidValues (String value)
        {
            ConfirmationCode confirmationCode = assertDoesNotThrow(
                () -> ConfirmationCode.of(value)
            );

            assertNotNull(confirmationCode);
            assertEquals(value, confirmationCode.getValue());
        }

        @Test
        @DisplayName("Should reject null value")
        void shouldRejectNullValue ()
        {
            InvalidConfirmationCodeException exception = assertThrows(
                InvalidConfirmationCodeException.class,
                () -> ConfirmationCode.of(null)
            );

            assertEquals(ConfirmationCodeError.IS_NULL, exception.getError());
        }

        @ValueSource(strings = {
            "",
            " ",
            "  "
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject value with invalid length")
        void shouldRejectBlankValue (String value)
        {
            InvalidConfirmationCodeException exception = assertThrows(
                InvalidConfirmationCodeException.class,
                () -> ConfirmationCode.of(value)
            );

            assertEquals(ConfirmationCodeError.IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithInvalidLength")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject values with invalid length")
        void shouldRejectValuesWithInvalidLength (String value)
        {
            InvalidConfirmationCodeException exception = assertThrows(
                InvalidConfirmationCodeException.class,
                () -> ConfirmationCode.of(value)
            );

            assertEquals(ConfirmationCodeError.HAS_INVALID_LENGTH, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithInvalidCharacters")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject values with invalid character")
        void shouldRejectValuesWithInvalidCharacters (String value)
        {
            InvalidConfirmationCodeException exception = assertThrows(
                InvalidConfirmationCodeException.class,
                () -> ConfirmationCode.of(value)
            );

            assertEquals(ConfirmationCodeError.HAS_INVALID_CHARACTERS, exception.getError());
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
            String value1 = "0".repeat(ConfirmationCodePolicy.LENGTH);

            ConfirmationCode confirmationCode1 = ConfirmationCode.from(value1);
            ConfirmationCode confirmationCode2 = ConfirmationCode.from(value1);

            assertEquals(confirmationCode1, confirmationCode2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "0".repeat(ConfirmationCodePolicy.LENGTH);
            String value2 = "1".repeat(ConfirmationCodePolicy.LENGTH);

            ConfirmationCode confirmationCode1 = ConfirmationCode.from(value1);
            ConfirmationCode confirmationCode2 = ConfirmationCode.from(value2);

            assertNotEquals(confirmationCode1, confirmationCode2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value1 = "0".repeat(ConfirmationCodePolicy.LENGTH);

            ConfirmationCode confirmationCode1 = ConfirmationCode.from(value1);
            ConfirmationCode confirmationCode2 = ConfirmationCode.from(value1);

            assertEquals(confirmationCode1.hashCode(), confirmationCode2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "0".repeat(ConfirmationCodePolicy.LENGTH);
            String value2 = "1".repeat(ConfirmationCodePolicy.LENGTH);

            ConfirmationCode confirmationCode1 = ConfirmationCode.from(value1);
            ConfirmationCode confirmationCode2 = ConfirmationCode.from(value2);

            assertNotEquals(confirmationCode1.hashCode(), confirmationCode2.hashCode());
        }
    }
}