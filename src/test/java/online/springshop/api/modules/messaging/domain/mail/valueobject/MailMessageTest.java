package online.springshop.api.modules.messaging.domain.mail.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mail message tests")
class MailMessageTest
{
    @Nested
    @DisplayName("Creation tests")
    class CreationTests
    {
        @Test
        @DisplayName("Should fail when subject is null")
        void shouldFailWhenSubjectIsNull ()
        {
            MailFrom from = new MailFrom("test1@example.com");
            MailRecipient recipient = new MailRecipient("test2@example.com");
            HtmlBody body = new HtmlBody("<html></html>");

            assertThrows(
                NullPointerException.class,
                () -> new MailMessage(
                    null,
                    from,
                    recipient,
                    body
                )
            );
        }

        @Test
        @DisplayName("Should fail when from is null")
        void shouldFailWhenFromIsNull ()
        {
            MailSubject subject = new MailSubject("Message subject");
            MailRecipient recipient = new MailRecipient("test2@example.com");
            HtmlBody body = new HtmlBody("<html></html>");

            assertThrows(
                NullPointerException.class,
                () -> new MailMessage(
                    subject,
                    null,
                    recipient,
                    body
                )
            );
        }

        @Test
        @DisplayName("Should fail when recipient is null")
        void shouldFailWhenRecipientIsNull ()
        {
            MailSubject subject = new MailSubject("Message subject");
            MailFrom from = new MailFrom("test1@example.com");
            HtmlBody body = new HtmlBody("<html></html>");

            assertThrows(
                NullPointerException.class,
                () -> new MailMessage(
                    subject,
                    from,
                    null,
                    body
                )
            );
        }

        @Test
        @DisplayName("Should fail when body is null")
        void shouldFailWhenBodyIsNull ()
        {
            MailSubject subject = new MailSubject("Message subject");
            MailFrom from = new MailFrom("test1@example.com");
            MailRecipient recipient = new MailRecipient("test2@example.com");

            assertThrows(
                NullPointerException.class,
                () -> new MailMessage(
                    subject,
                    from,
                    recipient,
                    null
                )
            );
        }

        @Test
        @DisplayName("Should create mail message")
        void shouldCreateMailMessage ()
        {
            MailSubject subject = new MailSubject("Message subject");
            MailFrom from = new MailFrom("test1@example.com");
            MailRecipient recipient = new MailRecipient("test2@example.com");
            HtmlBody body = new HtmlBody("<html></html>");

            MailMessage mailMessage = new MailMessage(
                subject,
                from,
                recipient,
                body
            );

            assertEquals(subject, mailMessage.subject());
            assertEquals(from, mailMessage.from());
            assertEquals(recipient, mailMessage.recipient());
            assertEquals(body, mailMessage.body());
        }
    }
}