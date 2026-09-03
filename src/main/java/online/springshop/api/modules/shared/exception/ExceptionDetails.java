package online.springshop.api.modules.shared.exception;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public final class ExceptionDetails
{
    private final String title;
    private final String message;
    private final int status;
    private final URI instance;
    private final URI type;

    @Singular("property")
    private Map<String, Object> properties;

    private ExceptionDetails(
        String title,
        String message,
        int status,
        URI instance,
        URI type,
        Map<String, Object> properties
    )
    {
        this.title = title;
        this.message = message;
        this.status = status;
        this.instance = instance;
        this.type = type;
        this.properties = properties;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties ()
    {
        return this.properties == null ? Map.of() : Map.copyOf(this.properties);
    }

    @JsonAnySetter
    public void setProperties (String key, Object value)
    {
        if (this.properties == null)
            this.properties = new LinkedHashMap<>();

        properties.put(key, value);
    }
}
