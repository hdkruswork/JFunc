package org.functional;

import java.util.Optional;
import java.util.function.Supplier;

public final class Lazy<T> {

    public static <A> Lazy<A> of(Supplier<A> supplier) {
        return new Lazy<>(supplier);
    }

    private T value;
    private boolean computed;
    private final Supplier<T> supplier;
    private final Object syncObj = new Object();

    private Lazy(Supplier<T> supplier) {
        this.value = null;
        this.computed = false;
        this.supplier = supplier;
    }

    public T get() {
        if (!computed) {
            synchronized (syncObj) {
                if (!computed) {
                    value = supplier.get();
                    computed = true;
                }
            }
        }

        return value;
    }

    public Optional<T> getIfComputed() {
        return computed
            ? Optional.ofNullable(value)
            : Optional.empty();
    }
}
