package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ProjectileComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class MovementSystem extends GameSystem {
    private final GameMap map;

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

        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);
            boolean isProjectile = entityManager.hasComponent(entity, ProjectileComponent.class);

            float radius = isProjectile ? 2f : 6f; 
            float speedMult = map.getSpeedMultiplier(t.x, t.y);
            
            float dx = v.vx * speedMult * deltaTime;
            float dy = v.vy * speedMult * deltaTime;

            // --- IMPROVED COLLISION WITH UNSTUCK LOGIC ---
            
            // Try X movement
            if (isWalkable(t.x + dx, t.y, radius)) {
                t.x += dx;
            } else {
                // If blocked, try to "slide" or check if we are already stuck
                if (!isWalkable(t.x, t.y, radius) && isWalkable(t.x + dx, t.y, radius * 0.5f)) {
                    t.x += dx; // Allow moving if it helps getting out
                }
                if (isProjectile) {
                    entityManager.removeEntity(entity);
                    continue;
                }
            }

            // Try Y movement
            if (isWalkable(t.x, t.y + dy, radius)) {
                t.y += dy;
            } else {
                if (!isWalkable(t.x, t.y, radius) && isWalkable(t.x, t.y + dy, radius * 0.5f)) {
                    t.y += dy;
                }
                if (isProjectile) {
                    entityManager.removeEntity(entity);
                }
            }
        }
    }

    private boolean isWalkable(float x, float y, float radius) {
        return map.isWalkable(x - radius, y - radius) &&
               map.isWalkable(x + radius, y - radius) &&
               map.isWalkable(x - radius, y + radius) &&
               map.isWalkable(x + radius, y + radius);
    }
}
