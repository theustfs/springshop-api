package online.springshop.api.modules.auth.domain.user.valueobject;

import online.springshop.api.modules.auth.domain.user.exception.EmailError;
import online.springshop.api.modules.auth.domain.user.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static online.springshop.api.fixtures.EmailFixtures.getMinimumValidDomain;
import static online.springshop.api.fixtures.EmailFixtures.getMinimumValidValue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email tests")
class EmailTest
{
    private static final String FIXTURES = "online.springshop.api.fixtures.EmailFixtures";

    @Nested
    @DisplayName("Validation tests")
    class ValidationTests
    {
        @Test
        @DisplayName("Should reject null values")
        void shouldRejectNullValues ()
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(null)
            );

            assertEquals(EmailError.IS_NULL, exception.getError());
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
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValidValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should accept valid values")
        void shouldAcceptValidValues (String value)
        {
            Email email = assertDoesNotThrow(
                () -> Email.of(value)
            );

            assertNotNull(email);
            assertEquals(value, email.getValue());
        }

        @MethodSource(FIXTURES + "#getTooLongValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too long values")
        void shouldRejectTooLongValues (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.IS_TOO_LONG, exception.getError());
        }

        @ValueSource(strings = {
            "ab",
            "a@@b.c"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject malformed values")
        void shouldRejectMalformedValues (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.IS_MALFORMED, exception.getError());
        }

        @ValueSource(strings = {
            "a @b.c",
            "a@b c.c",
            "a@\tb.c"
        })
        @ParameterizedTest(name = "{index}: ''{0}''")
        @DisplayName("Should reject values with whitespace")
        void shouldRejectValuesWithWhitespace (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.HAS_WHITESPACE, exception.getError());
        }

        @Test
        @DisplayName("Should reject blank local parts")
        void shouldRejectBlankLocalParts ()
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of("@" + getMinimumValidDomain())
            );

            assertEquals(EmailError.LOCAL_PART_IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getTooLongLocalPartValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too long local parts")
        void shouldRejectTooLongLocalParts (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.LOCAL_PART_IS_TOO_LONG, exception.getError());
        }

        @ValueSource(strings = {
            ",@b.c",
            ":@b.c",
            "é@b.c"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject local parts with invalid characters")
        void shouldRejectLocalPartsWithInvalidCharacters (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.LOCAL_PART_HAS_INVALID_CHARACTERS, exception.getError());
        }

        @ValueSource(strings = {
            ".a@b.c",
            "a.@b.c"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject local parts starting or ending with a dot")
        void shouldRejectLocalPartsStartingOrEndingWithDot (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.LOCAL_PART_START_OR_END_WITH_DOT, exception.getError());
        }

        @Test
        @DisplayName("Should reject local parts with consecutive dots")
        void shouldRejectLocalPartsWithConsecutiveDots ()
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of("a..a@" + getMinimumValidDomain())
            );

            assertEquals(EmailError.LOCAL_PART_HAS_CONSECUTIVE_DOTS, exception.getError());
        }

        @Test
        @DisplayName("Should reject blank domain parts")
        void shouldRejectBlankDomainParts ()
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of("a@")
            );

            assertEquals(EmailError.DOMAIN_PART_IS_BLANK, exception.getError());
        }

        @MethodSource(FIXTURES + "#getValuesWithTooFewDomainLabels")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should require a domain and a top-level domain")
        void shouldRequireDomainAndTopLevelDomain (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.DOMAIN_PART_HAS_TOO_FEW_LABELS, exception.getError());
        }

        @ValueSource(strings = {
            "a@.b.c",
            "a@b.c."
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject domain parts starting or ending with a dot")
        void shouldRejectDomainPartsStartingOrEndingWithDot (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.DOMAIN_PART_START_OR_END_WITH_DOT, exception.getError());
        }

        @Test
        @DisplayName("Should reject domain parts with consecutive dots")
        void shouldRejectDomainPartsWithConsecutiveDots ()
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of("a@b..c")
            );

            assertEquals(EmailError.DOMAIN_PART_HAS_CONSECUTIVE_DOTS, exception.getError());
        }

        @MethodSource(FIXTURES + "#getTooLongDomainLabelValues")
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject too long domain labels")
        void shouldRejectTooLongDomainLabels (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.DOMAIN_LABEL_IS_TOO_LONG, exception.getError());
        }

        @ValueSource(strings = {
            "a@_.c",
            "a@!.c",
            "a@é.c"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject domain labels with invalid characters")
        void shouldRejectDomainLabelsWithInvalidCharacters (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.DOMAIN_LABEL_HAS_INVALID_CHARACTERS, exception.getError());
        }

        @ValueSource(strings = {
            "a@-b.c",
            "a@b-.c"
        })
        @ParameterizedTest(name = "{index}: {0}")
        @DisplayName("Should reject domain labels starting or ending with a hyphen")
        void shouldRejectDomainLabelsStartingOrEndingWithHyphen (String value)
        {
            InvalidEmailException exception = assertThrows(
                InvalidEmailException.class,
                () -> Email.of(value)
            );

            assertEquals(EmailError.DOMAIN_LABEL_START_OR_END_WITH_HYPHEN, exception.getError());
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
            String value = getMinimumValidValue();

            Email email = Email.restore(value);

            assertNotNull(email);
            assertEquals(value, email.getValue());
        }

        @Test
        @DisplayName("Should not restore when value is null")
        void shouldNotRestoreWhenValueIsNull ()
        {
            assertThrows(
                NullPointerException.class,
                () -> Email.restore(null)
            );
        }
    }

    @Nested
    @DisplayName("Normalization tests")
    class NormalizationTests
    {
        @MethodSource(FIXTURES + "#getRawNormalizedValuePairs")
        @ParameterizedTest(name = "{argumentSetName}: ''{0}'' -> {1}")
        @DisplayName("Should normalize and store value")
        void shouldNormalizeAndStoreValue (String raw, String normalized)
        {
            Email email = Email.of(raw);

            assertNotNull(email);
            assertEquals(normalized, email.getValue());
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
            String value = getMinimumValidValue();

            Email email1 = Email.of(value);
            Email email2 = Email.of(value);

            assertEquals(email1, email2);
        }

        @Test
        @DisplayName("Should not be equal when values are not equal")
        void shouldNotBeEqualWhenValuesAreNotEqual ()
        {
            Email email1 = Email.of(getMinimumValidValue());
            Email email2 = Email.of("b@" + getMinimumValidDomain());

            assertNotEquals(email1, email2);
        }

        @Test
        @DisplayName("Hash code should be equal when values are equal")
        void hashCodeShouldBeEqualWhenValuesAreEqual ()
        {
            String value = getMinimumValidValue();

            Email email1 = Email.of(value);
            Email email2 = Email.of(value);

            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("Hash code should not be equal when values are not equal")
        void hashCodeShouldNotBeEqualWhenValuesAreNotEqual ()
        {
            Email email1 = Email.of(getMinimumValidValue());
            Email email2 = Email.of("b@" + getMinimumValidDomain());

            assertNotEquals(email1.hashCode(), email2.hashCode());
        }
    }
}
