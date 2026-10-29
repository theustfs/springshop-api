package online.springshop.api.testsupport;

import online.springshop.api.configuration.TestContainerConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataRedisTest
@ActiveProfiles("test")
@Import(TestContainerConfiguration.class)
public abstract class AbstractDataRedisTest
{

}
