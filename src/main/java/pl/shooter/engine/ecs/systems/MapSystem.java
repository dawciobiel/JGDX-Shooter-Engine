package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ColliderComponent;
import pl.shooter.engine.ecs.components.ProjectileComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Handles collisions between entities and the game map (walls).
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
            ColliderComponent collider = entityManager.getComponent(entity, ColliderComponent.class);
            
            float radius = (collider != null) ? collider.radius : 0f;

            // Check 4 points around the entity based on its radius
            if (!isAreaWalkable(transform.x, transform.y, radius)) {
                
                // If it's a projectile - destroy it on impact with wall
                if (entityManager.hasComponent(entity, ProjectileComponent.class)) {
                    entityManager.removeEntity(entity);
                    continue;
                }

                // If it's a living entity - push it back (very simple logic: revert movement)
                VelocityComponent velocity = entityManager.getComponent(entity, VelocityComponent.class);
                if (velocity != null) {
                    transform.x -= velocity.vx * deltaTime;
                    transform.y -= velocity.vy * deltaTime;
                }
            }
        }
    }

    /**
     * Checks if the circular area of an entity is clear of walls.
     */
    private boolean isAreaWalkable(float x, float y, float radius) {
        // Check center, top, bottom, left, right
        return currentMap.isWalkable(x, y) &&
               currentMap.isWalkable(x + radius, y) &&
               currentMap.isWalkable(x - radius, y) &&
               currentMap.isWalkable(x, y + radius) &&
               currentMap.isWalkable(x, y - radius);
    }
}
