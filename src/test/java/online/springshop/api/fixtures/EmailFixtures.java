package online.springshop.api.fixtures;

import online.springshop.api.modules.auth.domain.user.policy.EmailPolicy;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class EmailFixtures
{
    public static String getMinimumValidValue ()
    {
        return "a@" + getMinimumValidDomain();
    }

    public static String getMinimumValidDomain ()
    {
        return "a.".repeat(EmailPolicy.MIN_DOMAIN_LABELS - 1) + "a";
    }

    static Stream<String> getValidValues ()
    {
        int localPartLength = Math.min(
            EmailPolicy.MAX_LOCAL_PART_LENGTH,
            EmailPolicy.MAX_TOTAL_LENGTH - ("@" + getMinimumValidDomain()).length()
        );
        int domainLabelLength = Math.min(
            EmailPolicy.MAX_DOMAIN_LABEL_LENGTH,
            EmailPolicy.MAX_TOTAL_LENGTH
                - "a@".length()
                - ".a".repeat(EmailPolicy.MIN_DOMAIN_LABELS - 1).length()
        );

        return Stream.of(
            getMinimumValidValue(),
            "a".repeat(localPartLength) + "@" + getMinimumValidDomain(),
            "a@" + "a".repeat(domainLabelLength)
                + ".a".repeat(EmailPolicy.MIN_DOMAIN_LABELS - 1)
        );
    }

    static Stream<String> getTooLongValues ()
    {
        return Stream.of("a".repeat(EmailPolicy.MAX_TOTAL_LENGTH + 1));
    }

    static Stream<String> getTooLongLocalPartValues ()
    {
        return Stream.of(
            "a".repeat(EmailPolicy.MAX_LOCAL_PART_LENGTH + 1)
                + "@" + getMinimumValidDomain()
        );
    }

    static Stream<String> getTooLongDomainLabelValues ()
    {
        return Stream.of(
            "a@" + "a".repeat(EmailPolicy.MAX_DOMAIN_LABEL_LENGTH + 1)
                + ".a".repeat(EmailPolicy.MIN_DOMAIN_LABELS - 1)
        );
    }

    static Stream<String> getValuesWithTooFewDomainLabels ()
    {
        return Stream.of(
            "a@" + "a.".repeat(EmailPolicy.MIN_DOMAIN_LABELS - 2) + "a"
        );
    }

    static Stream<Arguments> getRawNormalizedValuePairs ()
    {
        return Stream.of(
            Arguments.argumentSet("lower case", getMinimumValidValue().toUpperCase(), getMinimumValidValue()),
            Arguments.argumentSet("trim", "  " + getMinimumValidValue() + "  ", getMinimumValidValue())
        );
    }
}
