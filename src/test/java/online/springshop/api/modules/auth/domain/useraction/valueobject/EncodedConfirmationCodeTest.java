package online.springshop.api.modules.auth.domain.useraction.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Encoded confirmation code tests")
class EncodedConfirmationCodeTest
{
    @Nested
    @DisplayName("Validation tests")
    class ValidationTests
    {
        @Test
        @DisplayName("Should accept valid value")
        void shouldAcceptValidValue ()
        {
            String value = "encodedConfirmationCode";

            EncodedConfirmationCode encodedConfirmationCode = assertDoesNotThrow(
                () -> EncodedConfirmationCode.from(value)
            );

            assertNotNull(encodedConfirmationCode);
            assertEquals(value, encodedConfirmationCode.getValue());
        }

        @Test
        @DisplayName("Should reject null value")
        void shouldRejectNullValue ()
        {
            assertThrows(
                NullPointerException.class,
                () -> EncodedConfirmationCode.from(null)
            );
        }

        @ValueSource(strings = {
            "",
            " ",
            "  "
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject blank values")
        void shouldRejectNullValues (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> EncodedConfirmationCode.from(value)
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
            String value1 = "encodedConfirmationCode1";

            EncodedConfirmationCode confirmationCode1 = EncodedConfirmationCode.from(value1);
            EncodedConfirmationCode confirmationCode2 = EncodedConfirmationCode.from(value1);

            assertEquals(confirmationCode1, confirmationCode2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "encodedConfirmationCode1";
            String value2 = "encodedConfirmationCode2";

            EncodedConfirmationCode confirmationCode1 = EncodedConfirmationCode.from(value1);
            EncodedConfirmationCode confirmationCode2 = EncodedConfirmationCode.from(value2);

            assertNotEquals(confirmationCode1, confirmationCode2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value1 = "encodedConfirmationCode1";

            EncodedConfirmationCode confirmationCode1 = EncodedConfirmationCode.from(value1);
            EncodedConfirmationCode confirmationCode2 = EncodedConfirmationCode.from(value1);

            assertEquals(confirmationCode1.hashCode(), confirmationCode2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "encodedConfirmationCode1";
            String value2 = "encodedConfirmationCode2";

            EncodedConfirmationCode confirmationCode1 = EncodedConfirmationCode.from(value1);
            EncodedConfirmationCode confirmationCode2 = EncodedConfirmationCode.from(value2);

            assertNotEquals(confirmationCode1.hashCode(), confirmationCode2.hashCode());
        }
    }
}