package online.springshop.api.modules.messaging.application.port;

import online.springshop.api.modules.messaging.domain.mail.valueobject.HtmlBody;

import java.util.Map;

/**
 * Application port for rendering an HTML template with named variables.
 * Implementations own template lookup and rendering-engine integration.
 */
public interface HtmlTemplateRenderer
{
    /**
     * Renders the identified template using the supplied variables.
     *
     * @param path the implementation-specific template path or name
     * @param variables variables made available to the template by name
     * @return the rendered HTML content
     * @throws RuntimeException when the template cannot be resolved or rendered
     */
    HtmlBody render (String path, Map<String, Object> variables);
}
