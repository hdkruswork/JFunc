package org.functional.collections;

import org.functional.Lazy;
import org.functional.Tuple;
import org.functional.Unit;

import java.util.*;
import java.util.function.*;

import static org.functional.Unit.unit;

public interface Stream<T> extends Iterable<T> {

    Optional<T> getHeadOption();
    Stream<T> getTail();

    boolean headIsComputed();
    boolean tailIsComputed();

    default Stream<T> append(final T item) {
        return getHeadOption()
            .map(head -> Streams.create(head, () -> getTail().append(item)))
            .orElse(Streams.create(item, Streams.empty()));
    }

    default Stream<T> append(final Stream<T> stream) {
        return getHeadOption()
            .map(head -> Streams.create(head, () -> getTail().append(stream)))
            .orElse(stream);
    }

    default Stream<T> append(final Supplier<Stream<T>> getStreamFunc) {
        return getHeadOption()
            .map(head -> Streams.create(head, () -> getTail().append(getStreamFunc)))
            .orElseGet(getStreamFunc);
    }

    default Stream<T> append(final Lazy<Stream<T>> lazyStream) {
        return getHeadOption()
            .map(head -> Streams.create(head, () -> getTail().append(lazyStream)))
            .orElse(lazyStream.get());
    }

    default <B> boolean corresponds(final Stream<B> other) {
        return corresponds(other, Objects::equals);
    }

    default <B> boolean corresponds(final Stream<B> other, BiFunction<T, B, Boolean> p) {
        return zip(other)
            .forAll(t -> p.apply(t.getItem1(), t.getItem2()));
    }

    default Stream<T> drop(final int count) {
        Stream<T> result = this;
        for (int i = 0; i < count && nonEmpty(); i++) {
            result = result.getTail();
        }

        return result;
    }

    default Stream<T> dropWhile(final Predicate<T> predicate) {
        return dropWhileIf(predicate, true);
    }

    default Stream<T> dropWhileNot(final Predicate<T> predicate) {
        return dropWhileIf(predicate, false);
    }

    default Stream<T> dropWhileIf(final Predicate<T> predicate, final boolean isTrue) {
        return foldLeftWhile(
            this,
            (r, it) -> predicate.test(it) == isTrue,
            (r, it) -> r.getTail()
        );
    }

    default boolean exist(final Predicate<T> predicate) {
        return existIf(predicate, true);
    }

    default boolean existNot(final Predicate<T> predicate) {
        return existIf(predicate, false);
    }

    default boolean existIf(final Predicate<T> predicate, boolean isTrue) {
        return foldLeftWhile(
            false,
            (r, it) -> r != isTrue,
            (r, it) -> predicate.test(it) == isTrue
        );
    }

    default Stream<T> filter(final Predicate<T> predicate) {
        return Streams.withFilter(this, predicate, true);
    }

    default Stream<T> filterNot(final Predicate<T> predicate) {
        return Streams.withFilter(this, predicate, false);
    }

    default Optional<T> first(final Predicate<T> predicate) {
        return nth(0, predicate);
    }

    default Optional<T> firstNot(final Predicate<T> predicate) {
        return nthNot(0, predicate);
    }

    default <R> Stream<R> flatMap(final Function<T, ? extends Iterable<R>> function) {
        return Streams.withFlatMapFunction(this, function);
    }

    default boolean forAll(final Predicate<T> predicate) {
        return foldLeftWhile(
            true,
            (r, it) -> r,
            (r, it) -> r && predicate.test(it)
        );
    }

    default void foreEach(final Consumer<T> consumer) {
        foldLeft(
            unit(),
            Unit.from((r, it) -> consumer.accept(it))
        );
    }

    default Unit forEach(final Function<T, Unit> consumer) {
        return foldLeft(unit(), (ignore, it) -> consumer.apply(it));
    }

    default int forEachWhile(final Predicate<T> predicate, final Consumer<T> consumer) {
        return forEachWhileIf(predicate, consumer, true);
    }

    default int forEachWhileNot(final Predicate<T> predicate, final Consumer<T> consumer) {
        return forEachWhileIf(predicate, consumer, false);
    }

    default int forEachWhileIf(
            final Predicate<T> predicate, final Consumer<T> consumer,
            final boolean isTrue
    ) {
        return foldLeftWhile(
            0,
            (r, it) -> predicate.test(it) == isTrue,
            (r, it) -> {
                consumer.accept(it);
                return r + 1;
            }
        );
    }

    default <R> R foldLeft(final R initialValue, final BiFunction<R, T, R> function) {
        return foldLeftWhile(initialValue, (r, it) -> true, function);
    }

    default <R> R foldLeftWhile(
            final R initialValue,
            final BiFunction<R, T, Boolean> predicate,
            final BiFunction<R, T, R> function
    ) {
        R result = initialValue;
        Stream<T> curr = this;
        while (
            curr.getHeadOption().isPresent()
            && predicate.apply(result, curr.getHeadOption().get())
        ) {
            result = function.apply(result, curr.getHeadOption().get());
            curr = curr.getTail();
        }

        return result;
    }

    default boolean isEmpty() {
        return !nonEmpty();
    }

    default boolean nonEmpty() {
        return getHeadOption().isPresent();
    }

    default Iterator<T> iterator() {

        final Stream<T> self = this;

        return new Iterator<T>() {

            private Stream<T> owner = self;

            @Override
            public boolean hasNext() {
                return owner.nonEmpty();
            }

            @Override
            public T next() {
                return owner
                    .getHeadOption()
                    .map(head -> {
                        owner = owner.getTail();
                        return head;
                    })
                    .orElse(null);
            }
        };
    }

    default <R> Stream<R> map(final Function<T, R> function) {
        return Streams.withMapFunction(this, function);
    }

    default Optional<T> nth(final int nth, final Predicate<T> predicate) {
        return nthIf(nth, predicate, true);
    }

    default Optional<T> nthNot(final int nth, final Predicate<T> predicate) {
        return nthIf(nth, predicate, false);
    }

    default Optional<T> nthIf(
            final int nth,
            final Predicate<T> predicate,
            final boolean isTrue
    ) {
        return Streams
            .withFilter(this, predicate, isTrue)
            .drop(nth)
            .getHeadOption();
    }

    default Stream<T> prepend(final T item) {
        return Streams.create(item, this);
    }

    default Stream<T> prepend(final Stream<T> stream) {
        return stream.append(this);
    }

    default List<T> take(final int count) {
        return foldLeftWhile(
            new ArrayList<>(count),
            (r, it) -> r.size() < count,
            (r, it) -> {
                r.add(it);
                return r;
            }
        );
    }

    default List<T> takeWhile(final Predicate<T> predicate) {
        return takeWhileIf(predicate, true);
    }

    default List<T> takeWhileNot(final Predicate<T> predicate) {
        return takeWhileIf(predicate, false);
    }

    default List<T> takeWhileIf(final Predicate<T> predicate, final boolean isTrue) {
        return foldLeftWhile(
            new LinkedList<>(),
            (r, it) -> predicate.test(it) == isTrue,
            (r, it) -> {
                r.addLast(it);
                return r;
            }
        );
    }

    default <W> Stream<Tuple<T, W>> zip(Stream<W> other) {
        return Streams.zipStreams(this, other);
    }

    default Stream<Tuple<T, Integer>> zipWithIndex() {
        return Streams.zipStreams(this, Streams.startingAt(0));
    }
}
