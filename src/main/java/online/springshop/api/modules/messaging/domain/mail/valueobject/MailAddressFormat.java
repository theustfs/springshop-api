package online.springshop.api.modules.messaging.domain.mail.valueobject;

import java.util.regex.Pattern;

final class MailAddressFormat
{
    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private MailAddressFormat ()
    {

    }

    static boolean isValid (String value)
    {
        return SIMPLE_EMAIL_PATTERN.matcher(value).matches();
    }
}
