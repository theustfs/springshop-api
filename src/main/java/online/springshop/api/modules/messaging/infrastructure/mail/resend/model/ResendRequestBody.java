package online.springshop.api.modules.messaging.infrastructure.mail.resend.model;

import lombok.Builder;
import online.springshop.api.modules.messaging.domain.mail.valueobject.MailMessage;

@Builder
public record ResendRequestBody (
    String from,
    String to,
    String subject,
    String html
)
{
    public static ResendRequestBody fromMailMessage (MailMessage mailMessage)
    {
        return new ResendRequestBody(
            mailMessage.from().value(),
            mailMessage.recipient().value(),
            mailMessage.subject().value(),
            mailMessage.body().value()
        );
    }
}
