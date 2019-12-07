package org.functional;

import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LazyTest {

    @SuppressWarnings("unchecked")
    private final Supplier<String> supplier = mock(Supplier.class);

    private Lazy<String> lazyValue;

    @Before
    public void setUp() {
        when(supplier.get()).thenReturn("hello");

        lazyValue = Lazy.of(supplier);
    }

    @Test
    public void testBeforeComputed() {
        // Then
        assertFalse(lazyValue.getIfComputed().isPresent());
        verify(supplier, never()).get();
    }

    @Test
    public void testAfterComputed() {
        // When
        final String value = lazyValue.get();

        // Then
        assertTrue(lazyValue.getIfComputed().isPresent());
        assertEquals("hello", value);
        verify(supplier, times(1)).get();
    }

    @Test
    public void testMemoization() {
        // When
        lazyValue.get();
        lazyValue.get();
        lazyValue.get();

        // Then
        verify(supplier, times(1)).get();
    }
}
