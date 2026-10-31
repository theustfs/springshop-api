package online.springshop.api.modules.auth.domain.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Encoded password tests")
class EncodedPasswordTest
{
    @Test
    @DisplayName("Should reject null value")
    void shouldRejectNullValue ()
    {
        assertThrows(
            NullPointerException.class,
            () -> EncodedPassword.from(null)
        );
    }

    @ValueSource(strings = {
        "",
        "  ",
        "   "
    })
    @ParameterizedTest(name = "{index}: ''{0}''")
    @DisplayName("Should reject blank values")
    void shouldRejectBlankValues (String value)
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> EncodedPassword.from(value)
        );
    }

    @Nested
    @DisplayName("Equals and hash code tests")
    class EqualsAndHashCodeTests
    {
        @Test
        @DisplayName("Should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual ()
        {
            String value = "encodedPassword";

            EncodedPassword encodedPassword1 = EncodedPassword.from(value);
            EncodedPassword encodedPassword2 = EncodedPassword.from(value);

            assertEquals(encodedPassword1, encodedPassword2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "encodedPassword";
            String value2 = "encoded_password";

            EncodedPassword encodedPassword1 = EncodedPassword.from(value1);
            EncodedPassword encodedPassword2 = EncodedPassword.from(value2);

            assertNotEquals(encodedPassword1, encodedPassword2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "encodedPassword";

            EncodedPassword encodedPassword1 = EncodedPassword.from(value);
            EncodedPassword encodedPassword2 = EncodedPassword.from(value);

            assertEquals(encodedPassword1.hashCode(), encodedPassword2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "encodedPassword";
            String value2 = "encoded_password";

            EncodedPassword encodedPassword1 = EncodedPassword.from(value1);
            EncodedPassword encodedPassword2 = EncodedPassword.from(value2);

            assertNotEquals(encodedPassword1.hashCode(), encodedPassword2.hashCode());
        }
    }
}