package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.StaticMap;

import java.util.List;

/**
 * Handles collisions with the environment and world boundaries.
 */
public class MapSystem extends GameSystem {
    private final GameMap currentMap;

    public MapSystem(EntityManager entityManager, GameMap map) {
        super(entityManager);
        this.currentMap = map;
    }

    @Override
    public void update(float deltaTime) {
        if (currentMap == null) return;

        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class);

        for (Entity entity : entities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            
            // 1. Handle boundaries if it's a static map
            if (currentMap instanceof StaticMap sm) {
                if (transform.x < 0) transform.x = 0;
                if (transform.x > sm.getWidth()) transform.x = sm.getWidth();
                if (transform.y < 0) transform.y = 0;
                if (transform.y > sm.getHeight()) transform.y = sm.getHeight();
            }

            // 2. Handle Wall Collisions (Primitive "slide" or "stop" logic)
            if (!currentMap.isWalkable(transform.x, transform.y)) {
                // If entity is inside a wall, push it back (very simple logic)
                // In a real engine, we would check future position in MovementSystem
            }
        }
    }
}
