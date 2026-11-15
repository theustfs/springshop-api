package online.springshop.api;

import online.springshop.api.configuration.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class})
public class SpringshopApplication
{
	public static void main (String[] args)
	{
		SpringApplication.run(SpringshopApplication.class, args);
	}
}
