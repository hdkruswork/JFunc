package org.functional.collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class StreamTest {

    private final Stream<Integer> naturals = Streams.startingAt(1);

    private final Stream<Integer> factorialStream = factorial();
    private final Stream<Integer> fibonacciStream = fibonacci();

    @Test
    public void testFactorial() {
        // Given
        final Stream<Integer> factorialSample =
            Streams.from(1, 2, 6, 24, 120, 720, 5040, 40320, 362880);

        // Then
        assertTrue(factorialStream.corresponds(factorialSample));
    }

    @Test
    public void testFibonacci() {
        // Given
        final Stream<Integer> fibSample =
            Streams.from(1, 1, 2, 3, 5, 8, 13, 21, 34);

        // Then
        assertTrue(fibonacciStream.corresponds(fibSample));
    }

    @Test
    public void testToStringOfComputedStream() {
        // Given
        final Stream<Integer> stream = Streams.from(0, 1, 2);

        // Then
        assertEquals("{0, 1, 2}", stream.toString());
    }

    @Test
    public void testToStringOfAppendedStream() {
        // Given
        final Stream<Integer> stream =
            Streams
                .from(0, 1)
                .append(3);

        // Then
        assertEquals("{0, ...}", stream.toString());
    }

    @Test
    public void testToStringOfPrependedStream() {
        // Given
        final Stream<Integer> stream =
            Streams
                .from(1, 2)
                .prepend(0);

        // Then
        assertEquals("{0, 1, 2}", stream.toString());
    }

    private Stream<Integer> factorial() {
        return Streams
            .create(
                1,
                () ->
                    factorialStream
                        .zip(naturals.getTail())
                        .map(t -> t.getItem1() * t.getItem2())
            );
    }

    private Stream<Integer> fibonacci() {
        return Streams
            .from(1, 1)
            .append(() ->
                fibonacciStream
                    .zip(fibonacciStream.getTail())
                    .map(t -> t.getItem1() + t.getItem2())
            );

    }
}
