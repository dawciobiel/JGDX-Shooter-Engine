package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class MovementSystem extends GameSystem {
    private final GameMap map;
    private static final float MAX_COLLISION_RADIUS = 14f;

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
        
        // Optimization: Get obstacles once per frame, not per entity
        List<Entity> obstacles = entityManager.getEntitiesWithComponents(
                TransformComponent.class, ColliderComponent.class, ObstacleComponent.class);

        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);
            ColliderComponent collider = entityManager.getComponent(entity, ColliderComponent.class);
            boolean isProjectile = entityManager.hasComponent(entity, ProjectileComponent.class);

            float rawRadius = (collider != null) ? collider.radius : (isProjectile ? 2f : 6f);
            float physicalRadius = Math.min(rawRadius, MAX_COLLISION_RADIUS);
            
            float dx = v.vx * v.terrainMultiplier * deltaTime;
            float dy = v.vy * v.terrainMultiplier * deltaTime;

            if (Math.abs(dx) < 0.001f && Math.abs(dy) < 0.001f) continue;

            // X Movement
            if (isAreaClear(t.x + dx, t.y, physicalRadius, entity, obstacles)) {
                t.x += dx;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
                continue;
            }

            // Y Movement
            if (isAreaClear(t.x, t.y + dy, physicalRadius, entity, obstacles)) {
                t.y += dy;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
            }
        }
    }

    private boolean isAreaClear(float x, float y, float radius, Entity movingEntity, List<Entity> obstacles) {
        // 1. Map collision (Static)
        if (!isMapWalkable(x, y, radius)) return false;

        // 2. Obstacle collision (Dynamic)
        for (Entity obstacle : obstacles) {
            if (obstacle.getId() == movingEntity.getId()) continue;
            if (isPlayerPushingObject(movingEntity, obstacle)) continue;

            TransformComponent ot = entityManager.getComponent(obstacle, TransformComponent.class);
            ColliderComponent oc = entityManager.getComponent(obstacle, ColliderComponent.class);

            float obstacleRadius = Math.min(oc.radius, MAX_COLLISION_RADIUS);
            float minDistance = radius + obstacleRadius;
            
            // Fast AABB check before expensive distance squared calculation
            if (Math.abs(x - ot.x) > minDistance || Math.abs(y - ot.y) > minDistance) continue;

            float dx = x - ot.x;
            float dy = y - ot.y;
            if ((dx * dx + dy * dy) < (minDistance * minDistance)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPlayerPushingObject(Entity e1, Entity e2) {
        boolean e1Player = entityManager.hasComponent(e1, PlayerComponent.class);
        boolean e2Pushable = entityManager.hasComponent(e2, PushableComponent.class);
        if (e1Player && e2Pushable) return true;

        boolean e2Player = entityManager.hasComponent(e2, PlayerComponent.class);
        boolean e1Pushable = entityManager.hasComponent(e1, PushableComponent.class);
        return e2Player && e1Pushable;
    }

    private boolean isMapWalkable(float x, float y, float radius) {
        return map.isWalkable(x, y) &&
               map.isWalkable(x + radius, y) &&
               map.isWalkable(x - radius, y) &&
               map.isWalkable(x, y + radius) &&
               map.isWalkable(x, y - radius);
    }
}
