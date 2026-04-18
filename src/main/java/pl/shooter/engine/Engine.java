package pl.shooter.engine;

import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The main orchestrator of the game engine.
 * It manages the lifecycle of systems, the entity manager, and the event bus.
 */
public class Engine {
    /** Global default for map files if not specified in config */
    public static final String DEFAULT_MAP_FILE_NAME = "map.json";

    private final EntityManager entityManager;
    private final EventBus eventBus;
    private final List<GameSystem> systems;
    
    // Performance profiling
    private final Map<String, Long> systemExecutionTimes = new LinkedHashMap<>();

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
        systemExecutionTimes.put(system.getClass().getSimpleName(), 0L);
    }

    /**
     * The main update loop called every frame.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public void update(float deltaTime) {
        for (GameSystem system : systems) {
            long startTime = System.nanoTime();
            system.update(deltaTime);
            long endTime = System.nanoTime();
            
            systemExecutionTimes.put(system.getClass().getSimpleName(), endTime - startTime);
        }
    }

    /**
     * Returns performance data for all systems.
     * Key: System Name, Value: Last execution time in nanoseconds.
     */
    public Map<String, Long> getPerformanceData() {
        return Collections.unmodifiableMap(systemExecutionTimes);
    }

    /**
     * Resize all systems.
     */
    public void resize(int width, int height) {
        for (GameSystem system : systems) {
            system.resize(width, height);
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

    public List<GameSystem> getSystems() {
        return Collections.unmodifiableList(systems);
    }
}
