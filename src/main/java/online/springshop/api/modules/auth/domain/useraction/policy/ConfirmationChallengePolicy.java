package online.springshop.api.modules.auth.domain.useraction.policy;

import java.time.Duration;

/** Domain policy governing challenge lifetime and confirmation attempts. */
public final class ConfirmationChallengePolicy
{
    /** Time for which an activated challenge remains valid. */
    public static final Duration DURATION = Duration.ofMinutes(15);

    /** Maximum number of code-comparison attempts allowed for one challenge. */
    public static final int MAX_ATTEMPTS = 3;

    private ConfirmationChallengePolicy () {}
}
