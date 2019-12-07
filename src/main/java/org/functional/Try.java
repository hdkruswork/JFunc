package org.functional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Try<T> {

    public static <A> Try<A> of(Supplier<A> supplier) {
        try {
            return success(supplier.get());
        }
        catch (Throwable throwable) {
            return failure(throwable);
        }
    }

    public static <A> Success<A> success(A result) {
        return Success.of(result);
    }

    public static <A> Try<A> failure(Throwable throwable) {
        return Failure.of(throwable);
    }

    public boolean isSuccess() {
        return toOptional().isPresent();
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public <R> Try<R> flatMap(final Function<T, Try<R>> function) {
        return isSuccess()
            ? function.apply(((Success<T>)this).getResult())
            : failure(((Failure<T>)this).getThrowable());
    }

    public <R> Try<R> map(final Function<T, R> function) {
        return isSuccess()
            ? of(() -> function.apply(((Success<T>)this).getResult()))
            : failure(((Failure<T>)this).getThrowable());
    }

    public abstract Optional<T> toOptional();

    public abstract Optional<Throwable> toThrowable();

    private Try() { }

    // Inner classes

    private static final class Success<T> extends Try<T> {

        public static <A> Success<A> of(A result) {
            return new Success<>(result);
        }

        private final T result;

        private Success(T result) {
            this.result = result;
        }

        public T getResult() {
            return result;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.ofNullable(result);
        }

        @Override
        public Optional<Throwable> toThrowable() {
            return Optional.empty();
        }
    }

    private static final class Failure<T> extends Try<T> {

        public static <A> Try<A> of(final Throwable throwable) {
            return new Failure<>(throwable);
        }

        private final Throwable throwable;

        private Failure(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> toThrowable() {
            return Optional.ofNullable(throwable);
        }
    }
}
