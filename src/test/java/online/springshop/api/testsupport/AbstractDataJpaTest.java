package online.springshop.api.testsupport;

import online.springshop.api.configuration.TestContainerConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestContainerConfiguration.class)
public abstract class AbstractDataJpaTest
{

}
