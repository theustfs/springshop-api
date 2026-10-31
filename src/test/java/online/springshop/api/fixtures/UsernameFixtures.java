package online.springshop.api.fixtures;

import online.springshop.api.modules.auth.domain.user.policy.UsernamePolicy;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UsernameFixtures
{
    private static final int TEST_CASE_RUNS = 1;

    // 64 characters long
    private static final String BASE_VALUE = "dolor aut dolore est nisi sed est quibusdam consectetur cupidita";

    private static String generateRandomUsername (int length)
    {
        return BASE_VALUE.substring(0, length)
            .replaceAll("\\s$", "a"); // replaces trailing space
    }

    static Stream<String> getValidValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
        {
            // generates a random number between UsernamePolicy.MIN_LENGTH and UsernamePolicy.MAX_LENGTH
            int randomLength = (int) (Math.random() * (UsernamePolicy.MAX_LENGTH - UsernamePolicy.MIN_LENGTH + 1) + UsernamePolicy.MIN_LENGTH);

            values.add(generateRandomUsername(randomLength));
        }

        return values.stream();
    }

    static Stream<String> getTooShortValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
            values.add(generateRandomUsername(UsernamePolicy.MIN_LENGTH - 1));

        return values.stream();
    }

    static Stream<String> getTooLongValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
            values.add(generateRandomUsername(UsernamePolicy.MAX_LENGTH + 1));

        return values.stream();
    }

    static Stream<Arguments> getRawNormalizedValuePair ()
    {
        List<Arguments> pairs = new ArrayList<>();

        pairs.add(Arguments.argumentSet("lower case", "UserName", "username"));
        pairs.add(Arguments.argumentSet("trim", "  username  ", "username"));
        pairs.add(Arguments.argumentSet("remove consecutive spaces", "user   name", "user name"));

        return pairs.stream();
    }
}
