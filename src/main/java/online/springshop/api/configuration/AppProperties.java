package online.springshop.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app")
public record AppProperties (
    RESEND RESEND,
    MAIL MAIL
)
{
    public record RESEND (
        String API_KEY,
        String BASE_URL
    )
    {

    }

    public record MAIL (
        String FROM
    )
    {

    }

}
