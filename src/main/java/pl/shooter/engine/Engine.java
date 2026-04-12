package pl.shooter.engine;

import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * The main orchestrator of the game engine.
 * It manages the lifecycle of systems, the entity manager, and the event bus.
 */
public class Engine {
    private final EntityManager entityManager;
    private final EventBus eventBus;
    private final List<GameSystem> systems;

    public Engine() {
        this.entityManager = new EntityManager();
        this.eventBus = new EventBus();
        this.systems = new ArrayList<>();
    }

    /**
     * Adds a new system to the engine.
     * Systems are updated in the order they are added.
     */
    public void addSystem(GameSystem system) {
        systems.add(system);
    }

    /**
     * The main update loop called every frame.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(float deltaTime) {
        for (GameSystem system : systems) {
            system.update(deltaTime);
        }
    }

    /**
     * Clean up all system resources.
     */
    public void dispose() {
        for (GameSystem system : systems) {
            system.dispose();
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
