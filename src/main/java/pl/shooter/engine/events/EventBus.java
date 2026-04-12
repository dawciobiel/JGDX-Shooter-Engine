package pl.shooter.engine.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central system for managing communication between decoupled systems.
 * Uses the Publisher-Subscriber pattern.
 */
public class EventBus {
    private final Map<Class<? extends Event>, List<EventHandler<?>>> handlers = new HashMap<>();

    /**
     * Subscribe to a specific event type.
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    /**
     * Publish an event to all interested subscribers.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventHandler<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (EventHandler<?> handler : eventHandlers) {
                ((EventHandler<T>) handler).handle(event);
            }
        }
    }

    /**
     * Unsubscribe all handlers for a specific event type.
     */
    public void unsubscribeAll(Class<? extends Event> eventType) {
        handlers.remove(eventType);
    }
}
