package online.springshop.api.fixtures;

import online.springshop.api.modules.auth.domain.user.policy.PasswordPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PasswordFixtures
{
    private static final int TEST_CASE_RUNS = 1;

    // 128 characters long
    private static final String BASE_VALUE = "8KkjVT2Zgk9G9vh0V65DrQy9tGog112FWWhFeY5C71vUPtdySyoD9RPWHkjxVCiS9fUzjhC0OWZZI6SXtcNpknVeZiP7220lSCEOT25d56BlG4yc4kOihXsHdN6g7yqE";

    private static String generateRandomPassword (int length)
    {
        return BASE_VALUE.substring(0, length);
    }

    public static String getMinimumLengthValue ()
    {
        return generateRandomPassword(PasswordPolicy.MIN_LENGTH);
    }

    private static String generateSequence (char initialCharacter, int length, int step)
    {
        StringBuilder sequence = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            sequence.append((char) (initialCharacter + i * step));

        return sequence.toString();
    }

    static Stream<String> getValidValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
        {
            // generates a random number between PasswordPolicy.MIN_LENGTH and PasswordPolicy.MAX_LENGTH
            int randomLength = (int) (Math.random() * (PasswordPolicy.MAX_LENGTH - PasswordPolicy.MIN_LENGTH + 1) + PasswordPolicy.MIN_LENGTH);

            values.add(generateRandomPassword(randomLength));
        }

        return values.stream();
    }

    static Stream<String> getTooShortValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
            values.add(generateRandomPassword(PasswordPolicy.MIN_LENGTH - 1));

        return values.stream();
    }

    static Stream<String> getTooLongValues ()
    {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < TEST_CASE_RUNS; i++)
            values.add(generateRandomPassword(PasswordPolicy.MAX_LENGTH + 1));

        return values.stream();
    }

    static Stream<String> getValuesWithWhitespace ()
    {
        String minimumLengthValue = getMinimumLengthValue();

        return Stream.of(
            minimumLengthValue + " ",
            minimumLengthValue + "\t",
            minimumLengthValue + "\n"
        );
    }

    static Stream<String> getValuesWithLongRepeatingCharacterSequence ()
    {
        int invalidSequenceLength = PasswordPolicy.MAX_SEQUENCE_OF_REPEATING_CHARACTER + 1;

        return Stream.of(
            getMinimumLengthValue() + "V".repeat(invalidSequenceLength),
            getMinimumLengthValue() + "1".repeat(invalidSequenceLength)
        );
    }

    static Stream<String> getValuesWithLongAlphabeticSequence ()
    {
        int invalidSequenceLength = PasswordPolicy.MAX_ALPHABETIC_SEQUENCE + 1;

        return Stream.of(
            getMinimumLengthValue() + generateSequence('a', invalidSequenceLength, 1),
            getMinimumLengthValue() + generateSequence('z', invalidSequenceLength, -1)
        );
    }

    static Stream<String> getValuesWithLongNumericSequence ()
    {
        int invalidSequenceLength = PasswordPolicy.MAX_NUMERIC_SEQUENCE + 1;

        return Stream.of(
            getMinimumLengthValue() + generateSequence('0', invalidSequenceLength, 1),
            getMinimumLengthValue() + generateSequence('9', invalidSequenceLength, -1)
        );
    }
}
