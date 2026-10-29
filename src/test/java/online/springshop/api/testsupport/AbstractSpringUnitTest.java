package online.springshop.api.testsupport;

import online.springshop.api.configuration.PropertyConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(
    classes = {
        PropertyConfiguration.class
    },
    initializers = {
        ConfigDataApplicationContextInitializer.class
    }
)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class AbstractSpringUnitTest
{

}
