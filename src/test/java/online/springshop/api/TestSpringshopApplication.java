package online.springshop.api;

import online.springshop.api.configuration.DevContainerConfiguration;
import org.springframework.boot.SpringApplication;

public class TestSpringshopApplication
{
    public static void main(String[] args)
    {
        SpringApplication
            .from(SpringshopApplication::main)
            .with(DevContainerConfiguration.class)
            .run(args);
    }
}
