package online.springshop.api.modules.messaging.infrastructure.mail.resend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResendResponseBody (
    UUID id
)
{

}
