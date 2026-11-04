package com.alikuxac.humannature.api.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class HumanNatureEventBus {
    private static final HumanNatureEventBus INSTANCE = new HumanNatureEventBus();

    public static HumanNatureEventBus getInstance() {
        return INSTANCE;
    }

    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void unregister(Class<T> eventClass, Consumer<T> listener) {
        List<Consumer<?>> list = listeners.get(eventClass);
        if (list != null) {
            list.remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T post(T event) {
        if (event == null) {
            return null;
        }
        List<Consumer<?>> list = listeners.get(event.getClass());
        if (list != null && !list.isEmpty()) {
            for (Consumer<?> listener : list) {
                ((Consumer<T>) listener).accept(event);
            }
        }
        return event;
    }

    public boolean hasListeners(Class<?> eventClass) {
        List<Consumer<?>> list = listeners.get(eventClass);
        return list != null && !list.isEmpty();
    }
}
