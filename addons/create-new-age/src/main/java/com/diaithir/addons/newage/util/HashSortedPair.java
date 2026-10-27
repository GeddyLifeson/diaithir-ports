package com.diaithir.addons.newage.util;

import java.util.Objects;

public class HashSortedPair<T> {
    private final T a, b;

    public HashSortedPair(T a, T b) {
        if (a.hashCode() > b.hashCode()) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HashSortedPair<?> key)
            return key.a.equals(a) && key.b.equals(b);

        return false;
    }
}
