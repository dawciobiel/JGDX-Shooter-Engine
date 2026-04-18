package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Handles entity movement and sliding collisions.
 * Allows entities to overlap with pushable objects to trigger physical interactions.
 */
public class MovementSystem extends GameSystem {
    private final GameMap map;
    private static final float MAX_PHYSICAL_RADIUS = 14f;

    public MovementSystem(EntityManager entityManager, GameMap map) {
        super(entityManager);
        this.map = map;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = entityManager.getEntitiesWithComponents(
                TransformComponent.class,
                VelocityComponent.class
        );
        
        List<Entity> obstacles = entityManager.getEntitiesWithComponents(
                TransformComponent.class, ColliderComponent.class, ObstacleComponent.class);

        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);
            ColliderComponent collider = entityManager.getComponent(entity, ColliderComponent.class);
            boolean isProjectile = entityManager.hasComponent(entity, ProjectileComponent.class);

            float rawRadius = (collider != null) ? collider.radius : (isProjectile ? 2f : 6f);
            float radius = Math.min(rawRadius, MAX_PHYSICAL_RADIUS);
            
            float dx = v.vx * v.terrainMultiplier * deltaTime;
            float dy = v.vy * v.terrainMultiplier * deltaTime;

            if (Math.abs(dx) < 0.001f && Math.abs(dy) < 0.001f) continue;

            // X Axis
            if (isAreaClear(t.x + dx, t.y, radius, entity, obstacles)) {
                t.x += dx;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
                continue;
            }

            // Y Axis
            if (isAreaClear(t.x, t.y + dy, radius, entity, obstacles)) {
                t.y += dy;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
            }
        }
    }

    private boolean isAreaClear(float x, float y, float radius, Entity movingEntity, List<Entity> obstacles) {
        if (!isMapAreaWalkable(x, y, radius)) return false;

        for (Entity obstacle : obstacles) {
            if (obstacle.getId() == movingEntity.getId()) continue;
            
            // NEW: If the object is pushable, we allow overlap so PushingSystem can handle it
            if (canEntityPushObject(movingEntity, obstacle)) continue;

            TransformComponent ot = entityManager.getComponent(obstacle, TransformComponent.class);
            ColliderComponent oc = entityManager.getComponent(obstacle, ColliderComponent.class);

            float obstacleRadius = Math.min(oc.radius, MAX_PHYSICAL_RADIUS);
            float minDistance = radius + obstacleRadius;
            
            if (Math.abs(x - ot.x) > minDistance || Math.abs(y - ot.y) > minDistance) continue;

            float distSq = (x - ot.x) * (x - ot.x) + (y - ot.y) * (y - ot.y);
            if (distSq < minDistance * minDistance) return false;
        }
        return true;
    }

    private boolean isMapAreaWalkable(float x, float y, float radius) {
        return map.isWalkable(x, y) &&
               map.isWalkable(x + radius, y) &&
               map.isWalkable(x - radius, y) &&
               map.isWalkable(x, y + radius) &&
               map.isWalkable(x, y - radius) &&
               map.isWalkable(x + radius * 0.7f, y + radius * 0.7f) &&
               map.isWalkable(x - radius * 0.7f, y + radius * 0.7f) &&
               map.isWalkable(x + radius * 0.7f, y - radius * 0.7f) &&
               map.isWalkable(x - radius * 0.7f, y - radius * 0.7f);
    }

    private boolean canEntityPushObject(Entity pusher, Entity object) {
        // Only allow pushing if the object has a PushableComponent
        PushableComponent pComp = entityManager.getComponent(object, PushableComponent.class);
        if (pComp == null) return false;

        // If it's a "player only" object, check if pusher is player
        if (pComp.playerOnly) {
            return entityManager.hasComponent(pusher, PlayerComponent.class);
        }

        // Otherwise, any entity with Velocity can try to push it
        return entityManager.hasComponent(pusher, VelocityComponent.class);
    }
}
