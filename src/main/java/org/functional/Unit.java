package org.functional;

import java.util.function.*;

public final class Unit {
    private static final Unit singleton = new Unit();

    public static Supplier<Unit> from(final Runnable runnable) {
        return () -> {
            runnable.run();
            return unit();
        };
    }

    public static <A> Function<A, Unit> from(final Consumer<A> consumer) {
        return a -> {
            consumer.accept(a);
            return unit();
        };
    }

    public static <A, B> BiFunction<A, B, Unit> from(final BiConsumer<A, B> consumer) {
        return (a, b) -> {
            consumer.accept(a, b);
            return unit();
        };
    }

    public static Unit unit() {
        return singleton;
    }

    private Unit() {}
}
