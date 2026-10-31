package online.springshop.api.modules.auth.domain.user.policy;

public final class EmailPolicy
{
    public static final int MAX_TOTAL_LENGTH = 254;
    public static final int MAX_LOCAL_PART_LENGTH = 64;
    public static final int MAX_DOMAIN_LABEL_LENGTH = 63;
    public static final int MIN_DOMAIN_LABELS = 2;

    private EmailPolicy () {}
}
