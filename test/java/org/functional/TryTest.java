package org.functional;

import org.junit.Test;

import static org.junit.Assert.*;

public class TryTest {

    @Test
    public void testTryOfIsSuccess() {
        // Given
        final Try<Integer> tryRes = Try.of(() -> 10);
        final int resultValue = tryRes.toOptional().orElse(0);

        // Then
        assertTrue(tryRes.isSuccess());
        assertFalse(tryRes.isFailure());
        assertEquals(10, resultValue);
    }

    @Test
    public void testTryOfIsFailure() {
        // Given
        final Try<Integer> tryRes = Try.of(() -> {
            throw new NullPointerException();
        });
        final Throwable throwable = tryRes.toThrowable().orElse(new Exception());

        // Then
        assertFalse(tryRes.isSuccess());
        assertTrue(tryRes.isFailure());
        assertTrue(throwable instanceof NullPointerException);
    }
}
