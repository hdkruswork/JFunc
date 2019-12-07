package org.functional;

public class Tuple<A1, A2> {

    private final A1 item1;
    private final A2 item2;

    private Tuple(A1 item1, A2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public A1 getItem1() {
        return item1;
    }

    public A2 getItem2() {
        return item2;
    }

    @Override
    public String toString() {
        return "(" + item1.toString() + ", " + item2.toString() + ")";
    }

    public static <A, B> Tuple<A, B> create(A a, B b) {
        return new Tuple<>(a, b);
    }
}
