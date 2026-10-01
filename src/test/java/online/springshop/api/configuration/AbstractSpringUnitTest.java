package online.springshop.api.configuration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class AbstractSpringUnitTest
{

}
