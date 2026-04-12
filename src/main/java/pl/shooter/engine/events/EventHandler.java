package pl.shooter.engine.events;

/**
 * Functional interface for handling specific events.
 * @param <T> The type of the event.
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {
    void handle(T event);
}
