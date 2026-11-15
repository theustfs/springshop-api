package online.springshop.api.modules.messaging.infrastructure.mail.resend.configuration;

import online.springshop.api.configuration.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class ResendConfiguration
{
    @Bean
    RestClient resendClient (
        AppProperties appProperties,
        RestClient.Builder builder
    )
    {
        return builder
            .baseUrl(appProperties.RESEND().BASE_URL())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, appProperties.RESEND().API_KEY())
            .build();
    }
}