package io.github.tr100000.researcher.api.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Minimal array-backed event, API compatible with the Fabric events the original mod used. */
public final class Event<T> {
    private final Class<T> type;
    private final Function<T[], T> invokerFactory;
    private final List<T> listeners = new ArrayList<>();
    private volatile T invoker;

    Event(Class<T> type, Function<T[], T> invokerFactory) {
        this.type = type;
        this.invokerFactory = invokerFactory;
        rebuild();
    }

    public synchronized void register(T listener) {
        listeners.add(listener);
        rebuild();
    }

    public T invoker() {
        return invoker;
    }

    @SuppressWarnings("unchecked")
    private void rebuild() {
        invoker = invokerFactory.apply(listeners.toArray((T[])Array.newInstance(type, 0)));
    }
}
