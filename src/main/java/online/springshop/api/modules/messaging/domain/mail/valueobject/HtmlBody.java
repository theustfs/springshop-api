package online.springshop.api.modules.messaging.domain.mail.valueobject;

/**
 * Non-blank HTML content used as the body of a mail message. Construction rejects null and blank
 * values.
 *
 * @param value the rendered HTML markup
 */
public record HtmlBody (
    String value
)
{
    /**
     * Creates an HTML mail body.
     *
     * @throws NullPointerException when {@code value} is {@code null}
     * @throws IllegalArgumentException when {@code value} is blank
     */
    public HtmlBody
    {
        if (value == null)
            throw new NullPointerException("Html body value must not be null");

        if (value.isBlank())
            throw new IllegalArgumentException("Html body value must not be blank");
    }

    @Override
    public boolean equals (Object object)
    {
        if (object == this)
            return true;

        if (!(object instanceof HtmlBody that))
            return false;

        return this.value.equals(that.value);
    }

    @Override
    public int hashCode ()
    {
        return value.hashCode();
    }
}
