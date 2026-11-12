package online.springshop.api.modules.messaging.domain.mail.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail from tests")
class MailFromTest
{
    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should fail when value is null")
        void shouldFailWhenValueIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> new MailFrom(null)
            );
        }

        @ValueSource(strings = {
            "",
            " ",
            "  "
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should fail when value is blank")
        void shouldFailWhenValueIsBlank (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> new MailFrom(value)
            );
        }

        @ValueSource(strings = {
            "test",
            "@example.com",
            "test@",
            "test@example",
            "test@@example.com",
            "test @example.com",
            "test@example .com"
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should fail when value has invalid email format")
        void shouldFailWhenValueHasInvalidEmailFormat (String value)
        {
            assertThrows(
                IllegalArgumentException.class,
                () -> new MailFrom(value)
            );
        }

        @ValueSource(strings = {
            "test@example.com",
            "test.user+confirmation@example.com",
            "test@notifications.example.com"
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should create mail from when value has valid email format")
        void shouldCreateMailFromWhenValueHasValidEmailFormat (String value)
        {
            MailFrom mailFrom = new MailFrom(value);

            assertEquals(value, mailFrom.value());
        }
    }

    @Nested
    @DisplayName("Equals and hashcode tests")
    class EqualsAndHashCodeTest
    {
        @Test
        @DisplayName("Should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual ()
        {
            String value = "test@example.com";

            MailFrom mailFrom1 = new MailFrom(value);
            MailFrom mailFrom2 = new MailFrom(value);

            assertEquals(mailFrom1, mailFrom2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "test1@example.com";
            String value2 = "test2@example.com";

            MailFrom mailFrom1 = new MailFrom(value1);
            MailFrom mailFrom2 = new MailFrom(value2);

            assertNotEquals(mailFrom1, mailFrom2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "test@example.com";

            MailFrom mailFrom1 = new MailFrom(value);
            MailFrom mailFrom2 = new MailFrom(value);

            assertEquals(mailFrom1.hashCode(), mailFrom2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "test1@example.com";
            String value2 = "test2@example.com";

            MailFrom mailFrom1 = new MailFrom(value1);
            MailFrom mailFrom2 = new MailFrom(value2);

            assertNotEquals(mailFrom1.hashCode(), mailFrom2.hashCode());
        }
    }
}
