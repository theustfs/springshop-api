package online.springshop.api.shared.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Result error tests")
class ResultErrorTest
{
    private enum TestErrorCode
    {
        INVALID_ACTION
    }

    @Test
    @DisplayName("Should reject null message")
    void shouldRejectNullErrorMessage ()
    {
        assertThrows(
            NullPointerException.class,
            () -> new ResultError<>(null, TestErrorCode.INVALID_ACTION)
        );
    }

    @Test
    @DisplayName("Should reject null code")
    void shouldRejectNullCode ()
    {
        assertThrows(
            NullPointerException.class,
            () -> new ResultError<TestErrorCode>("Error message", null)
        );
    }
}