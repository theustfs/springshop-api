package online.springshop.api.modules.messaging.application.usecase;

import com.fasterxml.uuid.Generators;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.application.port.HtmlTemplateRenderer;
import online.springshop.api.modules.messaging.application.port.MailDeliveryRepository;
import online.springshop.api.modules.messaging.application.port.MailSender;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.shared.event.account.AccountEmailUpdateStepOneEvent;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Send account email update step one confirmation mail use case unit tests")
class SendAccountEmailUpdateStepOneConfirmationMailUseCaseUnitTest
{
    @Mock
    private Clock appClock;

    @Mock
    private MailSender mailSender;

    @Mock
    private AppProperties appProperties;

    @Mock
    private HtmlTemplateRenderer htmlTemplateRenderer;

    @Mock
    private MailDeliveryRepository mailDeliveryRepository;

    @InjectMocks
    private SendAccountEmailUpdateStepOneConfirmationMailUseCase useCase;

    private final ArgumentCaptor<MailDelivery> mailDeliveryCaptor = ArgumentCaptor.forClass(MailDelivery.class);

    private static final AppProperties.MAIL APP_PROPERTIES_MAIL = new AppProperties.MAIL("no-reply@springshop.online");

    private static final Instant TIME = Instant.now();

    @Test
    @DisplayName("Should fail when event is null")
    void shouldFailWhenEventIsNull ()
    {
        assertThrows(
            NullPointerException.class,
            () -> useCase.sendMail(null)
        );
    }

    @Test
    @DisplayName("Should send mail")
    void shouldSendMail ()
    {
        AccountEmailUpdateStepOneEvent event = new AccountEmailUpdateStepOneEvent(
            new IdempotencyKey(Generators.timeBasedEpochGenerator().generate()),
            "test@example.com",
            "000000",
            Duration.ofMinutes(1)
        );

        String templatePath = "/email/account-email-update-step-one";
        Map<String, Object> variables = Map.of(
            "CONFIRMATION_CODE", event.confirmationCode(),
            "CODE_EXPIRATION", event.confirmationCodeExpiration().toMinutes()
        );
        HtmlBody htmlBody = new HtmlBody("<html></html>");

        MailMessage mailMessage = MailMessage.builder()
            .from(new MailFrom(APP_PROPERTIES_MAIL.FROM()))
            .recipient(new MailRecipient(event.accountEmail()))
            .subject(new MailSubject("Account email update confirmation"))
            .body(htmlBody)
            .build();

        when(appClock.instant()).thenReturn(TIME);
        when(appProperties.MAIL()).thenReturn(APP_PROPERTIES_MAIL);

        MailProviderId providerId = new MailProviderId(Generators.timeBasedEpochGenerator().generate());
        MailDelivery mailDelivery = MailDelivery.from(providerId, appClock);

        when(htmlTemplateRenderer.render(templatePath, variables)).thenReturn(htmlBody);
        when(mailSender.send(mailMessage, event.idempotencyKey())).thenReturn(providerId);

        useCase.sendMail(event);

        verify(htmlTemplateRenderer, times(1)).render(templatePath, variables);
        verify(mailSender, times(1)).send(mailMessage, event.idempotencyKey());
        verify(mailDeliveryRepository, times(1)).save(mailDeliveryCaptor.capture());

        MailDelivery capturedMailDelivery = mailDeliveryCaptor.getValue();

        assertEquals(mailDelivery.getProviderId(), capturedMailDelivery.getProviderId());
        assertEquals(TIME, capturedMailDelivery.getCreatedAt());
    }
}