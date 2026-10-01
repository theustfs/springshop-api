package online.springshop.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfiguration
{
    @Bean
    Clock appClock ()
    {
        return Clock.systemUTC();
    }
}
