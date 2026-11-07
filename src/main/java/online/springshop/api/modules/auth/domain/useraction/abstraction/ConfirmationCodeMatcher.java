package online.springshop.api.modules.auth.domain.useraction.abstraction;

import online.springshop.api.modules.auth.domain.useraction.valueobject.ConfirmationCode;
import online.springshop.api.modules.auth.domain.useraction.valueobject.EncodedConfirmationCode;

/**
 * Domain abstraction for comparing a raw confirmation code with its encoded
 * representation without coupling the domain to an encoding implementation.
 */
public interface ConfirmationCodeMatcher
{
    /**
     * @param raw the raw confirmation code
     * @param encoded the encoded confirmation code
     * @return {@code true} when the raw code matches the encoded value
     */
    boolean matches (ConfirmationCode raw, EncodedConfirmationCode encoded);
}
