package online.springshop.api.modules.messaging.domain.mail.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("Html body tests")
class HtmlBodyTest
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
                () -> new HtmlBody(null)
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
                () -> new HtmlBody(value)
            );
        }

        @Test
        @DisplayName("Should create html body")
        void shouldCreateHtmlBody ()
        {
            String value = "<html></html>";

            HtmlBody htmlBody = new HtmlBody(value);

            assertEquals(value, htmlBody.value());
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
            String value = "<html></html>";

            HtmlBody htmlBody1 = new HtmlBody(value);
            HtmlBody htmlBody2 = new HtmlBody(value);

            assertEquals(htmlBody1, htmlBody2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "<html>1</html>";
            String value2 = "<html>2</html>";

            HtmlBody htmlBody1 = new HtmlBody(value1);
            HtmlBody htmlBody2 = new HtmlBody(value2);

            assertNotEquals(htmlBody1, htmlBody2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = "<html></html>";

            HtmlBody htmlBody1 = new HtmlBody(value);
            HtmlBody htmlBody2 = new HtmlBody(value);

            assertEquals(htmlBody1.hashCode(), htmlBody2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            String value1 = "<html>1</html>";
            String value2 = "<html>3</html>";

            HtmlBody htmlBody1 = new HtmlBody(value1);
            HtmlBody htmlBody2 = new HtmlBody(value2);

            assertNotEquals(htmlBody1.hashCode(), htmlBody2.hashCode());
        }
    }
}