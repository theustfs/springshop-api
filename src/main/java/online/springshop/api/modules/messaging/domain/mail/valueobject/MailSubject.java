package online.springshop.api.modules.messaging.domain.mail.valueobject;

/**
 * Non-blank subject of a mail message. Construction rejects null and blank values.
 *
 * @param value the subject text
 */
public record MailSubject (
    String value
)
{
    /**
     * Creates a mail subject.
     *
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code value} is blank
     */
    public MailSubject
    {
        if (value == null)
            throw new NullPointerException("Mail subject value must not be null");

        if (value.isBlank())
            throw new IllegalArgumentException("Mail subject value must not be empty");
    }

    @Override
    public boolean equals (Object object)
    {
        if (object == this)
            return true;

        if (!(object instanceof MailSubject that))
            return false;

        return this.value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
