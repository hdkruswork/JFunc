package org.functional.collections;

import org.functional.Lazy;
import org.functional.Tuple;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class Streams {

    private static final EmptyStream<?> EMPTY = new EmptyStream<>();
    private static final StreamBuilder<?> EMPTY_BUILDER = new StreamBuilder<>();

    public static <A> StreamBuilder<A> newBuilder() {
        @SuppressWarnings("unchecked")
        final StreamBuilder<A> emptyBuilder = (StreamBuilder<A>) EMPTY_BUILDER;
        return emptyBuilder;
    }

    public static <A> Stream<A> empty() {
        @SuppressWarnings("unchecked")
        final Stream<A> empty = (Stream<A>) EMPTY;
        return empty;
    }

    public static <A> Stream<A> create(final A head, final Stream<A> tail){
        return new ConStream<>(head, tail);
    }

    public static <T> Stream<T> create(final T head, final Supplier<Stream<T>> getTailFunc) {
        return new LazyTailStream<>(head, getTailFunc);
    }

    public static <A> Stream<A> withFilter(
            final Stream<A> baseStream,
            final Predicate<A> predicate,
            final boolean isTrue
    ) {
        return baseStream.nonEmpty()
            ? new FilteredStream<>(baseStream, predicate, isTrue)
            : empty();
    }

    public static <A, R> Stream<R> withMapFunction(
            final Stream<A> baseStream,
            final Function<A, R> mapFunction
    ) {
        return baseStream.nonEmpty()
            ? new MappedStream<>(baseStream, mapFunction)
            : empty();
    }

    public static <A, R> Stream<R> withFlatMapFunction(
            final Stream<A> baseStream,
            final Function<A, ? extends Iterable<R>> mapFunction
    ) {
        return baseStream.nonEmpty()
            ? new FlatMappedStream<>(baseStream, mapFunction)
            : empty();
    }

    public static <A, B> Stream<Tuple<A, B>> zipStreams(
            final Stream<A> aStream,
            final Stream<B> bStream
    ) {
        return aStream.nonEmpty() && bStream.nonEmpty()
            ? new ZippedStream<>(aStream, bStream)
            : empty();
    }

    @SafeVarargs
    public static <A> Stream<A> from(final A... array) {
        Stream<A> stream = empty();
        for (int idx = array.length - 1; idx >= 0; idx--) {
            stream = create(array[idx], stream);
        }

        return stream;
    }

    public static <A> Stream<A> from(final Iterable<A> iterable) {
        StreamBuilder<A> builder = newBuilder();
        for (final A item: iterable) {
            builder = builder.append(item);
        }

        return builder.build();
    }

    public static Stream<Integer> startingAt(final int first) {
        return repeat(first, i -> i + 1);
    }

    public static Stream<BigInteger> startingAt(final BigInteger first) {
        return repeat(first, i -> i.add(BigInteger.valueOf(1)));
    }

    public static <A> Stream<A> repeat(final A initial, Function<A, A> f) {
        return create(initial, () -> repeat(f.apply(initial), f));
    }

    public static class StreamBuilder<A> {

        private final Lazy<Stream<A>> lazyBuiltStream;

        private StreamBuilder(final A rear, final StreamBuilder<A> init) {
            this.lazyBuiltStream = Lazy.of(() -> {
                if (init != null) {
                    create(rear, init.build());
                }

                return empty();
            });
        }

        private StreamBuilder() {
            this(null, null);
        }

        public StreamBuilder<A> append(A item) {
            return new StreamBuilder<>(item, this);
        }

        public Stream<A> build() {
            return lazyBuiltStream.get();
        }
    }

    // Private types

    private static class StreamStringUtil {

        public static String toString(final Stream<?> stream) {
            return toString(stream, new StringBuilder());
        }

        private static String toString(final Stream<?> stream, final StringBuilder sb) {
            sb.append("{");
            addItemsString(stream, sb, true);
            sb.append("}");
            return sb.toString();
        }

        private static void addItemsString(
            final Stream<?> stream,
            final StringBuilder sb,
            final boolean isFirstItem
        ) {
            final String unknownTerm = "...";
            if (stream.headIsComputed()) {
                stream
                    .getHeadOption()
                    .ifPresent(head -> {
                        addSeparatorIfNeeded(sb, isFirstItem);
                        sb.append(head.toString());

                        if (stream.tailIsComputed()) {
                            addItemsString(stream.getTail(), sb, false);
                        }
                        else {
                            addSeparatorIfNeeded(sb, false);
                            sb.append(unknownTerm);
                        }
                    });
            }
            else {
                addSeparatorIfNeeded(sb, isFirstItem);
                sb.append(unknownTerm);
            }
        }

        private static void addSeparatorIfNeeded(final StringBuilder sb, final boolean isFirstItem) {
            if (!isFirstItem) {
                sb.append(", ");
            }
        }
    }

    private static final class EmptyStream<T> implements Stream<T> {

        private EmptyStream() {}

        @Override
        public Optional<T> getHeadOption() {
            return Optional.empty();
        }

        @Override
        public Stream<T> getTail() {
            return this;
        }

        @Override
        public boolean headIsComputed() {
            return true;
        }

        @Override
        public boolean tailIsComputed() {
            return true;
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }

    private static abstract class NonEmptyStream<T> implements Stream<T> {
        public abstract T getHead();

        @Override
        public boolean headIsComputed() {
            return true;
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }

    private static final class ConStream<T> extends NonEmptyStream<T> {

        private final T head;
        private final Stream<T> tail;

        private ConStream(final T head, final Stream<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public T getHead() {
            return head;
        }

        @Override
        public Optional<T> getHeadOption() {
            return Optional.ofNullable(head);
        }

        @Override
        public Stream<T> getTail() {
            return tail;
        }

        @Override
        public boolean tailIsComputed() {
            return true;
        }
    }

    private static final class LazyTailStream<T> extends NonEmptyStream<T> {

        private final T head;
        private final Lazy<Stream<T>> lazyTail;

        private LazyTailStream(final T head, final Supplier<Stream<T>> getTailFunc) {
            this(head, Lazy.of(getTailFunc));
        }

        private LazyTailStream(final T head, final Lazy<Stream<T>> lazyTail) {
            this.head = head;
            this.lazyTail = lazyTail;
        }

        @Override
        public T getHead() {
            return head;
        }

        @Override
        public Optional<T> getHeadOption() {
            return Optional.ofNullable(head);
        }

        @Override
        public Stream<T> getTail() {
            return lazyTail.get();
        }

        @Override
        public boolean tailIsComputed() {
            return lazyTail.getIfComputed().isPresent();
        }
    }

    private static final class FilteredStream<T> implements Stream<T> {

        private final Lazy<Optional<T>> lazyHead;
        private final Lazy<Stream<T>> lazyTail;

        private FilteredStream(final Stream<T> baseStream, final Predicate<T> predicate, final boolean isTrue) {

            final Lazy<Stream<T>> streamWithFirstMatch = Lazy.of(() ->
                baseStream.foldLeftWhile(
                    baseStream,
                    (r, it) -> predicate.test(it) != isTrue,
                    (r, it) -> r.getTail()
                )
            );

            lazyHead = Lazy.of(() -> streamWithFirstMatch.get().getHeadOption());
            lazyTail = Lazy.of(() -> withFilter(streamWithFirstMatch.get().getTail(), predicate, isTrue));
        }

        @Override
        public Optional<T> getHeadOption() {
            return lazyHead.get();
        }

        @Override
        public Stream<T> getTail() {
            return lazyTail.get();
        }

        @Override
        public boolean headIsComputed() {
            return lazyHead.getIfComputed().isPresent();
        }

        @Override
        public boolean tailIsComputed() {
            return lazyTail.getIfComputed().isPresent();
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }

    private static final class MappedStream<T, R> implements Stream<R> {

        private final Lazy<Optional<R>> lazyHead;
        private final Lazy<Stream<R>> lazyTail;

        private MappedStream(final Stream<T> baseStream, final Function<T, R> mapFunction) {
            this.lazyHead = Lazy.of(() -> baseStream.getHeadOption().map(mapFunction));
            lazyTail = Lazy.of(() -> withMapFunction(baseStream.getTail(), mapFunction));
        }

        @Override
        public Optional<R> getHeadOption() {
            return lazyHead.get();
        }

        @Override
        public Stream<R> getTail() {
            return lazyTail.get();
        }

        @Override
        public boolean headIsComputed() {
            return lazyHead.getIfComputed().isPresent();
        }

        @Override
        public boolean tailIsComputed() {
            return lazyTail.getIfComputed().isPresent();
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }

    private static final class FlatMappedStream<T, R> implements Stream<R> {

        private final Lazy<Optional<R>> lazyHead;
        private final Lazy<Stream<R>> lazyTail;

        private FlatMappedStream(final Stream<T> baseStream, final Function<T, ? extends Iterable<R>> mapFunction) {
            final Lazy<Stream<R>> lazyFirstStream = Lazy.of(() -> {
                final Iterable<R> firstIterable = baseStream.getHeadOption()
                    .<Iterable<R>>map(mapFunction)
                    .orElse(Collections.emptyList());

                return from(firstIterable);
            });

            lazyHead = Lazy.of(() -> lazyFirstStream.get().getHeadOption());
            lazyTail = Lazy.of(() ->
                lazyFirstStream.get()
                    .getTail()
                    .append(() -> withFlatMapFunction(baseStream.getTail(), mapFunction))
            );
        }

        @Override
        public Optional<R> getHeadOption() {
            return lazyHead.get();
        }

        @Override
        public Stream<R> getTail() {
            return lazyTail.get();
        }

        @Override
        public boolean headIsComputed() {
            return lazyHead.getIfComputed().isPresent();
        }

        @Override
        public boolean tailIsComputed() {
            return lazyTail.getIfComputed().isPresent();
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }

    private static final class ZippedStream<A, B> implements Stream<Tuple<A, B>> {

        private final Stream<A> aStream;
        private final Stream<B> bStream;

        private ZippedStream(final Stream<A> aStream, final Stream<B> bStream) {
            this.aStream = aStream;
            this.bStream = bStream;
        }

        @Override
        public Optional<Tuple<A, B>> getHeadOption() {
            return aStream
                .getHeadOption()
                .flatMap(a -> bStream
                    .getHeadOption()
                    .map(b -> Tuple.create(a, b))
                );
        }

        @Override
        public Stream<Tuple<A, B>> getTail() {
            return aStream.getTail().zip(bStream.getTail());
        }

        @Override
        public boolean headIsComputed() {
            return aStream.headIsComputed() && bStream.headIsComputed();
        }

        @Override
        public boolean tailIsComputed() {
            return aStream.tailIsComputed() && bStream.tailIsComputed();
        }

        @Override
        public String toString() {
            return StreamStringUtil.toString(this);
        }
    }
}
