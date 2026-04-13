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

            // Radius for map collision (8f is safe for 32px tiles)
            float radius = 8f; 
            
            if (isProjectile) radius = 2f;

            float speedMult = map.getSpeedMultiplier(t.x, t.y);
            
            // Try X move
            float nextX = t.x + (v.vx * speedMult * deltaTime);
            if (isWalkable(nextX, t.y, radius)) {
                t.x = nextX;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
                continue;
            }

            // Try Y move
            float nextY = t.y + (v.vy * speedMult * deltaTime);
            if (isWalkable(t.x, nextY, radius)) {
                t.y = nextY;
            } else if (isProjectile) {
                entityManager.removeEntity(entity);
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
