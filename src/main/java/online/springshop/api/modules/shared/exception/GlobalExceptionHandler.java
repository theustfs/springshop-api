package online.springshop.api.modules.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ExceptionDetails handleMethodArgumentNotValidException (
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    )
    {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
            );

        return ExceptionDetails.builder()
            .title("Invalid request body fields")
            .message("One or more fields are invalid")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .property("fields", errors)
            .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ExceptionDetails handleHttpMessageNotReadableException (
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Http message not readable")
            .message("Missing or malformed request body")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    ExceptionDetails handleBadCredentialsException (
        BadCredentialsException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Bad credentials")
            .message("Incorrect email or password")
            .status(HttpServletResponse.SC_UNAUTHORIZED)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ExceptionDetails handleDataIntegrityViolationException (
        DataIntegrityViolationException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Data integrity violation")
            .message("Operation violates database integrity")
            .status(HttpServletResponse.SC_CONFLICT)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    ExceptionDetails handleMissingRequestCookieException (
        MissingRequestCookieException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Missing required cookie")
            .message("Cookie %s is missing".formatted(exception.getCookieName()))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ExceptionDetails handleMissingRequestHeaderException (
        MissingRequestHeaderException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Missing required header")
            .message("Header %s is missing".formatted(exception.getHeaderName()))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ExceptionDetails handleMissingServletRequestParameterException (
        MissingServletRequestParameterException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Missing required parameter")
            .message("Parameter %s is missing".formatted(exception.getParameterName()))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ExceptionDetails handleMethodArgumentTypeMismatchException (
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    )
    {
        String parameter = exception.getPropertyName();

        String expectedType = exception.getRequiredType() != null ?
            exception.getRequiredType().getSimpleName() : "null";

        String receivedType = exception.getValue() != null ?
            exception.getValue().getClass().getSimpleName() : "null";

        return ExceptionDetails.builder()
            .title("Argument type mismatch")
            .message("Expected type for parameter %s is %s, instead got %s".formatted(parameter, expectedType, receivedType))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ExceptionDetails handleHttpRequestMethodNotSupportedException (
        HttpRequestMethodNotSupportedException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Method not supported")
            .message("Method %s is not supported by this instance".formatted(exception.getMethod()))
            .status(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ExceptionDetails handleNoResourceFoundException (
        NoResourceFoundException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Resource not found")
            .message("Requested resource could not be found")
            .status(HttpServletResponse.SC_NOT_FOUND)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }

    @ExceptionHandler(Exception.class)
    ExceptionDetails handleException (
        Exception exception,
        HttpServletRequest request
    )
    {
        LOGGER.error(exception.getMessage(), exception);

        return ExceptionDetails.builder()
            .title("Internal server error")
            .message("Un unexpected error occurred while processing the request")
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .instance(URI.create(request.getRequestURI()))
            .build();
    }
}
