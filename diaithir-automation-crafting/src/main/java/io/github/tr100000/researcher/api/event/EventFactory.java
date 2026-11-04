package io.github.tr100000.researcher.api.event;

import java.util.function.Function;

public final class EventFactory {
    private EventFactory() {}

    public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
        @SuppressWarnings("unchecked")
        Class<T> cast = (Class<T>)type;
        return new Event<>(cast, invokerFactory);
    }
}
