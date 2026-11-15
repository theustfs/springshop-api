package online.springshop.api.modules.messaging.infrastructure.mail.resend;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.modules.messaging.application.port.MailSender;
import online.springshop.api.modules.messaging.infrastructure.mail.resend.model.ResendRequestBody;
import online.springshop.api.modules.messaging.infrastructure.mail.resend.model.ResendResponseBody;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class MailSenderResendAdapter implements MailSender
{
    private final RestClient resendClient;

    @Override
    public MailProviderId send (MailMessage mailMessage, IdempotencyKey idempotencyKey)
    {
        if (mailMessage == null)
            throw new NullPointerException("Mail message must not be null");

        if (idempotencyKey == null)
            throw new NullPointerException("Idempotency key must not be null");

        ResendRequestBody requestBody = ResendRequestBody.fromMailMessage(mailMessage);

        ResponseEntity<ResendResponseBody> response = resendClient.post()
            .header("Idempotency-Key", idempotencyKey.value().toString())
            .body(requestBody)
            .retrieve()
            .toEntity(ResendResponseBody.class);

        if (response.getBody() == null)
            throw new RuntimeException("No response body received from resend API");

        if (response.getBody().id() == null)
            throw new RuntimeException("Invalid response body received from resend API");

        return new MailProviderId(response.getBody().id());
    }
}
