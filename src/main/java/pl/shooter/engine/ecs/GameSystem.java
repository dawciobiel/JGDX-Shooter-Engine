package pl.shooter.engine.ecs;

/**
 * Base class for all game systems that process entities every frame.
 */
public abstract class GameSystem {
    protected final EntityManager entityManager;

    public GameSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Update the system logic.
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    public abstract void update(float deltaTime);

    /**
     * Optional cleanup of system resources.
     */
    public void dispose() {
        // Default implementation does nothing
    }

    /**
     * Optional reaction to window resize.
     */
    public void resize(int width, int height) {
        // Default implementation does nothing
    }
}
