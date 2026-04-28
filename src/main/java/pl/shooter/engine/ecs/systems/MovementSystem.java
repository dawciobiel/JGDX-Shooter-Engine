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
        
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);

            // Apply movement based on velocity already filtered by LocalAvoidanceSystem
            t.x += v.vx * v.terrainMultiplier * deltaTime;
            t.y += v.vy * v.terrainMultiplier * deltaTime;
        }
    }
}
