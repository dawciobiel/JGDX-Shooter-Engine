package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;

import java.util.List;

/**
 * Updates positions of all entities based on their velocity.
 */
public class MovementSystem extends GameSystem {

    public MovementSystem(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = entityManager.getEntitiesWithComponents(
                TransformComponent.class,
                VelocityComponent.class
        );

        for (Entity entity : entities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent velocity = entityManager.getComponent(entity, VelocityComponent.class);

            transform.x += velocity.vx * deltaTime;
            transform.y += velocity.vy * deltaTime;
        }
    }
}
