package online.springshop.api.modules.messaging.application.usecase;

import lombok.RequiredArgsConstructor;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.application.port.HtmlTemplateRenderer;
import online.springshop.api.modules.messaging.application.port.MailDeliveryRepository;
import online.springshop.api.modules.messaging.application.port.MailSender;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.shared.event.account.AccountRegisteredEvent;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendAccountRegistrationConfirmationMailUseCase
{
    private final Clock appClock;
    private final MailSender mailSender;
    private final AppProperties appProperties;
    private final HtmlTemplateRenderer htmlTemplateRenderer;
    private final MailDeliveryRepository mailDeliveryRepository;

    public void sendMail (AccountRegisteredEvent event)
    {
        if (event == null)
            throw new NullPointerException("Event must not be null");

        HtmlBody htmlBody = htmlTemplateRenderer.render(
            "/email/account-registration",
            Map.of(
                "CONFIRMATION_CODE", event.confirmationCode(),
                "CODE_EXPIRATION", event.confirmationCodeExpiration().toMinutes()
            )
        );

        MailMessage mailMessage = MailMessage.builder()
            .from(new MailFrom(appProperties.MAIL().FROM()))
            .recipient(new MailRecipient(event.accountEmail()))
            .subject(new MailSubject("Account registration confirmation"))
            .body(htmlBody)
            .build();

        MailProviderId providerId = mailSender.send(mailMessage, event.idempotencyKey());
        MailDelivery mailDelivery = MailDelivery.from(providerId, appClock);

        mailDeliveryRepository.save(mailDelivery);
    }
}
