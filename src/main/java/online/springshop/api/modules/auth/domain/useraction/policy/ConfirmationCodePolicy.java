package online.springshop.api.modules.auth.domain.useraction.policy;

/** Domain policy for raw confirmation-code values. */
public final class ConfirmationCodePolicy
{
    /** Required number of decimal digits in a raw confirmation code. */
    public static final int LENGTH = 6;

    private ConfirmationCodePolicy () {}
}
