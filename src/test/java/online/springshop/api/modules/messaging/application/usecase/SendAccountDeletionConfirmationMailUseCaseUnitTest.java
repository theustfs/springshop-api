package online.springshop.api.modules.messaging.application.usecase;

import com.fasterxml.uuid.Generators;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.application.port.HtmlTemplateRenderer;
import online.springshop.api.modules.messaging.application.port.MailDeliveryRepository;
import online.springshop.api.modules.messaging.application.port.MailSender;
import online.springshop.api.modules.messaging.domain.mail.MailDelivery;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.shared.event.account.AccountDeletionEvent;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Send account deletion confirmation mail use case unit tests")
class SendAccountDeletionConfirmationMailUseCaseUnitTest
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
    private SendAccountDeletionConfirmationMailUseCase useCase;

    private static final DateTimeFormatter TIME_FORMATTER =
        DateTimeFormatter.ofPattern("MMMM dd',' yyyy 'at' hh:mm xxx", Locale.ENGLISH);

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
        Instant cancellationPeriod = Instant.now();

        AccountDeletionEvent event = new AccountDeletionEvent(
            new IdempotencyKey(Generators.timeBasedEpochGenerator().generate()),
            "test@example.com",
            "000000",
            Duration.ofMinutes(1),
            cancellationPeriod
        );

        String templatePath = "/email/account-deletion";
        Map<String, Object> variables = Map.of(
            "CONFIRMATION_CODE", event.confirmationCode(),
            "CODE_EXPIRATION", event.confirmationCodeExpiration().toMinutes(),
            "CANCELLATION_PERIOD", event.cancellationPeriod().atZone(ZoneOffset.UTC).format(TIME_FORMATTER)
        );
        HtmlBody htmlBody = new HtmlBody("<html></html>");

        MailMessage mailMessage = MailMessage.builder()
            .from(new MailFrom(APP_PROPERTIES_MAIL.FROM()))
            .recipient(new MailRecipient(event.accountEmail()))
            .subject(new MailSubject("Account deletion confirmation"))
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