package online.springshop.api.modules.auth.domain.user.policy;

public final class PasswordPolicy
{
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final int MAX_SEQUENCE_OF_REPEATING_CHARACTER = 2;
    public static final int MAX_ALPHABETIC_SEQUENCE = 2;
    public static final int MAX_NUMERIC_SEQUENCE = 2;

    private PasswordPolicy () {}
}
