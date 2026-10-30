package online.springshop.api.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDetails> handleMethodArgumentNotValidException (
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    )
    {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach((fieldError) -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ExceptionDetails.builder()
            .title("Invalid request fields")
            .message("One or more request fields are invalid")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .property("errors", errors)
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionDetails> handleConstraintViolationException (
        ConstraintViolationException exception,
        HttpServletRequest request
    )
    {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
            errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage())
        );

        return ExceptionDetails.builder()
            .title("Validation failed")
            .message("One or more request parameters are invalid")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .property("errors", errors)
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionDetails> handleHttpMessageNotReadableException (
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Message not readable")
            .message("The request body is missing or malformed")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionDetails> handleBadCredentialsException (
        BadCredentialsException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Bad credentials")
            .message("Incorrect username or password")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionDetails> handleHandlerMethodValidationException (
        HandlerMethodValidationException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Validation failed")
            .message("One or more request parameters are invalid")
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionDetails> handleMethodArgumentTypeMismatchException (
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    )
    {
        String expectedType = exception.getRequiredType() != null ? exception.getRequiredType().toString() : "void";

        return ExceptionDetails.builder()
            .title("Invalid parameter type")
            .message("Expected type for request parameter %s is %s".formatted(exception.getPropertyName(), expectedType))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionDetails> handleMissingServletRequestParameterException (
        MissingServletRequestParameterException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Missing request parameter")
            .message("A required request parameter %s is missing".formatted(exception.getParameterName()))
            .status(HttpServletResponse.SC_BAD_REQUEST)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionDetails> handleNoResourceFoundException (
        NoResourceFoundException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Resource not found")
            .message("The requested resource could not be found")
            .status(HttpServletResponse.SC_NOT_FOUND)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionDetails> handleHttpRequestMethodNotSupportedException (
        HttpRequestMethodNotSupportedException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Method not allowed")
            .message("The request method %s is not supported for this resource".formatted(request.getMethod()))
            .status(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionDetails> handleHttpMediaTypeNotSupportedException (
        HttpMediaTypeNotSupportedException exception,
        HttpServletRequest request
    )
    {
        return ExceptionDetails.builder()
            .title("Unsupported media type")
            .message("The request content type is not supported")
            .status(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDetails> handleDataIntegrityViolationException (
        DataIntegrityViolationException exception,
        HttpServletRequest request
    )
    {
        LOGGER.warn("A data integrity constraint was violated");

        return ExceptionDetails.builder()
            .title("Conflict")
            .message("The request conflicts with the current state of the resource")
            .status(HttpServletResponse.SC_CONFLICT)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDetails> handleException (
        Exception exception,
        HttpServletRequest request
    )
    {
        LOGGER.error(exception.getMessage(), exception);

        return ExceptionDetails.builder()
            .title("Internal server error")
            .message("An unexpected error occurred while processing the request")
            .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            .instance(URI.create(request.getRequestURI()))
            .build()
            .toResponseEntity();
    }
}
