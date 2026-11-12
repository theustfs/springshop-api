package online.springshop.api.modules.messaging.domain.mail.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail subject tests")
class MailSubjectTest
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
                () -> new MailSubject(null)
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
                () -> new MailSubject(value)
            );
        }

        @Test
        @DisplayName("Should create mail subject")
        void shouldCreateMailSubject ()
        {
            String value = "Mail subject";

            MailSubject mailSubject = new MailSubject(value);

            assertEquals(value, mailSubject.value());
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
            String value = "Mail subject";

            MailSubject mailSubject1 = new MailSubject(value);
            MailSubject mailSubject2 = new MailSubject(value);

            assertEquals(mailSubject1, mailSubject2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "Mail subject 1";
            String value2 = "Mail subject 2";

            MailSubject mailSubject1 = new MailSubject(value1);
            MailSubject mailSubject2 = new MailSubject(value2);

            assertNotEquals(mailSubject1, mailSubject2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "Mail subject";

            MailSubject mailSubject1 = new MailSubject(value);
            MailSubject mailSubject2 = new MailSubject(value);

            assertEquals(mailSubject1.hashCode(), mailSubject2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "Mail subject 1";
            String value2 = "Mail subject 2";

            MailSubject mailSubject1 = new MailSubject(value1);
            MailSubject mailSubject2 = new MailSubject(value2);

            assertNotEquals(mailSubject1.hashCode(), mailSubject2.hashCode());
        }
    }
}