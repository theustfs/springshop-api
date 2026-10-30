package online.springshop.api.shared.exception;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

/**
 * Describes an HTTP API error returned by the global exception handler.
 *
 * <p>The fixed fields identify the error category, provide a safe client-facing
 * message, expose the HTTP status code, and identify the request path where the
 * error occurred. Additional context can be supplied through {@code properties};
 * Jackson serializes those entries as top-level JSON fields.</p>
 *
 * <p>Instances are immutable and should be created with {@link #builder()}.</p>
 */
@Getter
@Builder
public class ExceptionDetails
{
    private final String title;
    private final String message;
    private final int status;
    private final URI instance;

    @Singular
    private final Map<String, Object> properties;

    private ExceptionDetails (
        String title,
        String message,
        int status,
        URI instance,
        Map<String, Object> properties
    )
    {
        this.title = title;
        this.message = message;
        this.status = status;
        this.instance = instance;
        this.properties = Map.copyOf(properties);
    }

    /**
     * Creates an HTTP response whose status and body come from this error.
     *
     * @return a response containing this instance as its body
     */
    public ResponseEntity<ExceptionDetails> toResponseEntity ()
    {
        return ResponseEntity.status(status)
            .body(this);
    }

    /**
     * Returns immutable extension properties that are flattened into the JSON response.
     *
     * @return additional error details keyed by their JSON field names
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties ()
    {
        return properties;
    }
}
