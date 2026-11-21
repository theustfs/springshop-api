package online.springshop.api.modules.messaging.application.usecase;

import lombok.RequiredArgsConstructor;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.application.port.HtmlTemplateRenderer;
import online.springshop.api.modules.messaging.application.port.MailDeliveryRepository;
import online.springshop.api.modules.messaging.application.port.MailSender;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.shared.event.account.AccountDeletionEvent;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendAccountDeletionConfirmationMailUseCase
{
    private final Clock appClock;
    private final MailSender mailSender;
    private final AppProperties appProperties;
    private final HtmlTemplateRenderer htmlTemplateRenderer;
    private final MailDeliveryRepository mailDeliveryRepository;

    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMMM dd',' yyyy 'at' hh:mm xxx", Locale.ENGLISH);

    public void sendMail (AccountDeletionEvent event)
    {
        if (event == null)
            throw new NullPointerException("Event must not be null");

        HtmlBody htmlBody = htmlTemplateRenderer.render(
            "/email/account-deletion",
            Map.of(
                "CONFIRMATION_CODE", event.getConfirmationCode(),
                "CODE_EXPIRATION", event.getConfirmationCodeExpiration().toMinutes(),
                "CANCELLATION_PERIOD", event.getCancellationPeriod().atZone(ZoneOffset.UTC).format(TIME_FORMATTER)
            )
        );

        MailMessage mailMessage = MailMessage.builder()
            .from(new MailFrom(appProperties.MAIL().FROM()))
            .recipient(new MailRecipient(event.getAccountEmail()))
            .subject(new MailSubject("Account deletion confirmation"))
            .body(htmlBody)
            .build();

        MailProviderId providerId = mailSender.send(mailMessage, event.getIdempotencyKey());
        MailDelivery mailDelivery = MailDelivery.from(providerId, appClock);

        mailDeliveryRepository.save(mailDelivery);
    }
}
