package online.springshop.api.modules.messaging.domain.mail.valueobject;

/**
 * Sender email address of a mail message.
 *
 * <p>The address is checked using the mail domain's intentionally simple format validation; this
 * value object does not attempt full RFC email-address validation. Construction rejects null,
 * blank, and incorrectly formatted values.</p>
 *
 * @param value the sender email address
 */
public record MailFrom (
    String value
)
{
    /**
     * Creates a sender email address.
     *
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code value} is blank or has an invalid email format
     */
    public MailFrom
    {
        if (value == null)
            throw new NullPointerException("Mail from value must not be null");

        if (value.isBlank())
            throw new IllegalArgumentException("Mail from value must not be empty");

        if (!MailAddressFormat.isValid(value))
            throw new IllegalArgumentException("Mail from value must have a valid email format");
    }

    @Override
    public boolean equals (Object object)
    {
        if (object == this)
            return true;

        if (!(object instanceof MailFrom that))
            return false;

        return this.value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
