package online.springshop.api.modules.messaging.infrastructure.template.thymeleaf;

import lombok.RequiredArgsConstructor;
import online.springshop.api.modules.messaging.domain.mail.valueobject.HtmlBody;
import online.springshop.api.modules.messaging.application.port.HtmlTemplateRenderer;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HtmlTemplateRendererThymeleafAdapter implements HtmlTemplateRenderer
{
    private final TemplateEngine thymeleafTemplateEngine;

    @Override
    public HtmlBody render (String path, Map<String, Object> variables)
    {
        Context context = new Context();
        context.setVariables(variables);

        return new HtmlBody(thymeleafTemplateEngine.process(path, context));
    }
}
