package online.springshop.api.modules.messaging.application.port;

import com.github.javafaker.Faker;
import online.springshop.api.modules.messaging.domain.mail.valueobject.HtmlBody;
import online.springshop.api.modules.messaging.infrastructure.template.thymeleaf.HtmlTemplateRendererThymeleafAdapter;
import online.springshop.api.modules.messaging.infrastructure.template.thymeleaf.configuration.ThymeleafConfiguration;
import online.springshop.api.testsupport.AbstractSpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    ThymeleafConfiguration.class,
    HtmlTemplateRendererThymeleafAdapter.class
})
@DisplayName("Html template renderer compliance tests")
class HtmlBodyTemplateRendererTest extends AbstractSpringUnitTest
{
    @Autowired
    private HtmlTemplateRenderer htmlTemplateRenderer;

    private static final Faker FAKER = new Faker();

    private static final String TEST_TEMPLATE_STRING = """
        <!DOCTYPE htmlBody>
        <htmlBody>
        <body>
        <p><span>%s</span></p>
        <p><span>%s</span></p>
        </body>
        </htmlBody>
        """;

    @Test
    @DisplayName("Should render template")
    void shouldRenderTemplate ()
    {
        String var1 = FAKER.gameOfThrones().character();
        String var2 = FAKER.harryPotter().character();

        Map<String, Object> variables = new HashMap<>();

        variables.put("VAR1", var1);
        variables.put("VAR2", var2);

        HtmlBody htmlBody = htmlTemplateRenderer.render("test_template", variables);

        assertEquals(
            TEST_TEMPLATE_STRING.formatted(var1, var2),
            htmlBody.value()
        );
    }

    @Test
    @DisplayName("Should fail when template does not exist")
    void shouldFailWhenTemplateDoesNotExist ()
    {
        String var1 = FAKER.gameOfThrones().character();
        String var2 = FAKER.harryPotter().character();

        Map<String, Object> variables = new HashMap<>();

        variables.put("VAR1", var1);
        variables.put("VAR2", var2);

        Exception exception = assertThrows(
            Exception.class,
            () -> htmlTemplateRenderer.render("file", variables)
        );

        assertInstanceOf(FileNotFoundException.class, exception.getCause());
    }
}
