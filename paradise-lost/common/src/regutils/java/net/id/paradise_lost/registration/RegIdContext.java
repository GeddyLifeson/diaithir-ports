package net.id.paradise_lost.registration;

import java.util.function.Supplier;

/**
 * 26.1 requires every Block/Item.Properties to carry the registry id before construction.
 * The registration provider records the id being registered here; mixins on the Properties
 * classes read it when the id was not set explicitly.
 */
public final class RegIdContext {
    public static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private RegIdContext() {
    }

    public static <T> Supplier<T> wrap(String name, Supplier<? extends T> supplier) {
        return () -> {
            String previous = CURRENT.get();
            CURRENT.set(name);
            try {
                return supplier.get();
            } finally {
                if (previous == null) CURRENT.remove();
                else CURRENT.set(previous);
            }
        };
    }
}
