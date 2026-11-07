package online.springshop.api.fixtures;

import online.springshop.api.modules.auth.domain.useraction.policy.ConfirmationCodePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfirmationCodeFixtures
{
    private static final int TEST_CASE_RUNS = 1;

    static Stream<String> getValidValues ()
    {
        return Stream.of("0".repeat(ConfirmationCodePolicy.LENGTH));
    }

    static Stream<String> getValuesWithInvalidLength ()
    {
        List<String> values = new ArrayList<>();

        values.add("0".repeat(ConfirmationCodePolicy.LENGTH - 1));
        values.add("0".repeat(ConfirmationCodePolicy.LENGTH + 1));

        return values.stream();
    }

    static Stream<String> getValuesWithInvalidCharacters ()
    {
        List<String> values = new ArrayList<>();

        String baseValue = "0".repeat(ConfirmationCodePolicy.LENGTH - 1);

        values.add(baseValue + "a");
        values.add(baseValue + "z");
        values.add(baseValue + "!");
        values.add(baseValue + "$");

        return values.stream();
    }
}
