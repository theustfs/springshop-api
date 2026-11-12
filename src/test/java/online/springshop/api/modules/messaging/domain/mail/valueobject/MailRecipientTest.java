package online.springshop.api.modules.messaging.domain.mail.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail recipient tests")
class MailRecipientTest
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
                () -> new MailRecipient(null)
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
                () -> new MailRecipient(value)
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
                () -> new MailRecipient(value)
            );
        }

        @ValueSource(strings = {
            "test@example.com",
            "test.user+confirmation@example.com",
            "test@notifications.example.com"
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should create mail recipient when value has valid email format")
        void shouldCreateMailRecipientWhenValueHasValidEmailFormat (String value)
        {
            MailRecipient mailRecipient = new MailRecipient(value);

            assertEquals(value, mailRecipient.value());
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

            MailRecipient mailRecipient1 = new MailRecipient(value);
            MailRecipient mailRecipient2 = new MailRecipient(value);

            assertEquals(mailRecipient1, mailRecipient2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "test1@example.com";
            String value2 = "test2@example.com";

            MailRecipient mailRecipient1 = new MailRecipient(value1);
            MailRecipient mailRecipient2 = new MailRecipient(value2);

            assertNotEquals(mailRecipient1, mailRecipient2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "test@example.com";

            MailRecipient mailRecipient1 = new MailRecipient(value);
            MailRecipient mailRecipient2 = new MailRecipient(value);

            assertEquals(mailRecipient1.hashCode(), mailRecipient2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "test1@example.com";
            String value2 = "test2@example.com";

            MailRecipient mailRecipient1 = new MailRecipient(value1);
            MailRecipient mailRecipient2 = new MailRecipient(value2);

            assertNotEquals(mailRecipient1.hashCode(), mailRecipient2.hashCode());
        }
    }
}
