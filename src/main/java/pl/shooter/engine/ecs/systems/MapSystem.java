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
 * Handles collisions between entities and the game map (walls) and applies terrain speed modifiers.
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

        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, VelocityComponent.class);

        for (Entity entity : entities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent velocity = entityManager.getComponent(entity, VelocityComponent.class);
            ColliderComponent collider = entityManager.getComponent(entity, ColliderComponent.class);
            
            float radius = (collider != null) ? collider.radius : 0f;

            // 1. Terrain Speed Modification
            // Calculate speed multiplier based on the tile at entity's center
            float terrainMultiplier = currentMap.getSpeedMultiplier(transform.x, transform.y);
            
            // 2. Projectile handling
            if (entityManager.hasComponent(entity, ProjectileComponent.class)) {
                if (!isAreaWalkable(transform.x, transform.y, radius)) {
                    entityManager.removeEntity(entity);
                }
                continue;
            }

            // 3. Character Sliding Logic & Velocity Apply
            // Apply terrain multiplier to the step
            float stepX = (velocity.vx * terrainMultiplier) * deltaTime;
            float nextX = transform.x + stepX;
            if (isAreaWalkable(nextX, transform.y, radius)) {
                transform.x = nextX;
            }

            float stepY = (velocity.vy * terrainMultiplier) * deltaTime;
            float nextY = transform.y + stepY;
            if (isAreaWalkable(transform.x, nextY, radius)) {
                transform.y = nextY;
            }
        }
    }

    private boolean isAreaWalkable(float x, float y, float radius) {
        float offset = radius * 0.707f;
        return currentMap.isWalkable(x, y) &&
               currentMap.isWalkable(x + radius, y) &&
               currentMap.isWalkable(x - radius, y) &&
               currentMap.isWalkable(x, y + radius) &&
               currentMap.isWalkable(x, y - radius) &&
               currentMap.isWalkable(x + offset, y + offset) &&
               currentMap.isWalkable(x - offset, y + offset) &&
               currentMap.isWalkable(x + offset, y - offset) &&
               currentMap.isWalkable(x - offset, y - offset);
    }
}
