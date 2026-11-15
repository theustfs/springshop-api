package online.springshop.api.modules.messaging.infrastructure.mail.resend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import online.springshop.api.configuration.AppProperties;
import online.springshop.api.modules.messaging.domain.mail.MailProviderId;
import online.springshop.api.modules.messaging.domain.mail.valueobject.*;
import online.springshop.api.modules.messaging.infrastructure.mail.resend.configuration.ResendConfiguration;
import online.springshop.api.modules.messaging.infrastructure.mail.resend.model.ResendRequestBody;
import online.springshop.api.shared.idempotency.IdempotencyKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles("test")
@Import(ResendConfiguration.class)
@RestClientTest(MailSenderResendAdapter.class)
@DisplayName("Mail sender resend adapter tests")
class MailSenderResendAdapterTest
{
    @Autowired
    private MailSenderResendAdapter resend;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppProperties appProperties;

    private final MailMessage MAIL_MESSAGE = MailMessage.builder()
        .from(new MailFrom("api@example.com"))
        .recipient(new MailRecipient("test@example.com"))
        .subject(new MailSubject("Mail message title"))
        .body(new HtmlBody("HTML body"))
        .build();

    private final IdempotencyKey IDEMPOTENCY_KEY = new IdempotencyKey(Generators.timeBasedEpochGenerator().generate());

    @Nested
    @DisplayName("Mail sending tests")
    class SendMailTests
    {
        private void assertRequestAndReturnResponse (
            String responseToReturn
        ) throws JsonProcessingException
        {
            String request = objectMapper.writeValueAsString(ResendRequestBody.fromMailMessage(MAIL_MESSAGE));

            server.expect(requestTo(appProperties.RESEND().BASE_URL()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(request))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header(HttpHeaders.AUTHORIZATION, appProperties.RESEND().API_KEY()))
                .andExpect(header("Idempotency-key", IDEMPOTENCY_KEY.value().toString()))
                .andRespond(withSuccess(responseToReturn, MediaType.APPLICATION_JSON));
        }

        private void assertRequestWithoutResponse () throws JsonProcessingException
        {
            String request = objectMapper.writeValueAsString(ResendRequestBody.fromMailMessage(MAIL_MESSAGE));

            server.expect(requestTo(appProperties.RESEND().BASE_URL()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(request))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header(HttpHeaders.AUTHORIZATION, appProperties.RESEND().API_KEY()))
                .andExpect(header("Idempotency-key", IDEMPOTENCY_KEY.value().toString()))
                .andRespond(withSuccess());
        }

        @Test
        @DisplayName("Should fail when mail message is null")
        void shouldFailWhenMailMessageIsNull ()
        {
            server.expect(ExpectedCount.never(), requestTo(appProperties.RESEND().BASE_URL()));

            assertThrows(
                NullPointerException.class,
                () -> resend.send(null, IDEMPOTENCY_KEY)
            );

            server.verify();
        }

        @Test
        @DisplayName("Should fail when idempotency key is null")
        void shouldFailWhenIdempotencyKeyIsNull ()
        {
            server.expect(ExpectedCount.never(), requestTo(appProperties.RESEND().BASE_URL()));

            assertThrows(
                NullPointerException.class,
                () -> resend.send(MAIL_MESSAGE, null)
            );

            server.verify();
        }

        @Test
        @DisplayName("Should fail when no response is received")
        void shouldFailWhenNoResponseIsReceived () throws JsonProcessingException
        {
            assertRequestWithoutResponse();

            assertThrows(
                RuntimeException.class,
                () -> resend.send(MAIL_MESSAGE, IDEMPOTENCY_KEY)
            );

            server.verify();
        }

        @Test
        @DisplayName("Should fail when response id is null")
        void shouldFailWhenResponseIdIsNull () throws JsonProcessingException
        {
            String response = "{}";

            assertRequestAndReturnResponse(response);

            assertThrows(
                RuntimeException.class,
                () -> resend.send(MAIL_MESSAGE, IDEMPOTENCY_KEY)
            );

            server.verify();
        }

        @Test
        @DisplayName("Should send mail message")
        void shouldSendMailMessage () throws JsonProcessingException
        {
            String response = "{\"id\": \"00000000-0000-0000-0000-000000000001\"}";

            assertRequestAndReturnResponse(response);

            MailProviderId mailId = resend.send(MAIL_MESSAGE, IDEMPOTENCY_KEY);

            assertEquals("00000000-0000-0000-0000-000000000001", mailId.value().toString());
            server.verify();
        }
    }
}
