package online.springshop.api.shared.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result tests")
class ResultTest
{
    private enum TestErrorCode
    {
        INVALID_ACTION
    }

    @Test
    @DisplayName("Should create success result")
    void shouldCreateSuccessResult ()
    {
        Result<String, TestErrorCode> result = Result.success("created");

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("created", result.value());
        assertNull(result.error());
    }

    @Test
    @DisplayName("Should create failure result")
    void shouldCreateFailureResult ()
    {
        String errorMessage = "Error message";
        TestErrorCode errorCode = TestErrorCode.INVALID_ACTION;

        ResultError<TestErrorCode> error = new ResultError<>(errorMessage, errorCode);

        Result<String, TestErrorCode> result = Result.failure(error);

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertNull(result.value());
        assertEquals(error, result.error());
        assertEquals(errorMessage, result.error().message());
        assertEquals(errorCode, result.error().code());
    }

    @Test
    @DisplayName("Should reject null value on success")
    void shouldRejectNullValueOnSuccess ()
    {
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, TestErrorCode>success(null)
        );
    }

    @Test
    @DisplayName("Should create success result with Void value")
    void shouldCreateSuccessResultWithVoidValue ()
    {
        Result<Void, TestErrorCode> result = Result.success();

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertNull(result.value());
        assertNull(result.error());
    }

    @Test
    @DisplayName("Should reject null error on failure")
    void shouldRejectNullErrorOnFailure ()
    {
        assertThrows(
            NullPointerException.class,
            () -> Result.<String, TestErrorCode>failure(null)
        );
    }
}
