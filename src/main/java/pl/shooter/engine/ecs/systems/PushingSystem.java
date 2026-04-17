package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Handles physical interaction (pushing) between entities.
 */
public class PushingSystem extends GameSystem {
    private final GameMap map;

    public PushingSystem(EntityManager entityManager, GameMap map) {
        super(entityManager);
        this.map = map;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> pushables = entityManager.getEntitiesWithComponents(
                PushableComponent.class, TransformComponent.class, ColliderComponent.class, VelocityComponent.class);
        
        List<Entity> players = entityManager.getEntitiesWithComponents(
                PlayerComponent.class, TransformComponent.class, ColliderComponent.class);

        if (pushables.isEmpty() || players.isEmpty()) return;

        for (Entity player : players) {
            TransformComponent pt = entityManager.getComponent(player, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(player, ColliderComponent.class);

            for (Entity pushable : pushables) {
                PushableComponent pComp = entityManager.getComponent(pushable, PushableComponent.class);
                
                // Only player can push if playerOnly is true
                if (pComp.playerOnly && !entityManager.hasComponent(player, PlayerComponent.class)) continue;

                TransformComponent tt = entityManager.getComponent(pushable, TransformComponent.class);
                ColliderComponent tc = entityManager.getComponent(pushable, ColliderComponent.class);
                VelocityComponent tv = entityManager.getComponent(pushable, VelocityComponent.class);

                float dx = tt.x - pt.x;
                float dy = tt.y - pt.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float minDistance = pc.radius + tc.radius;

                if (distance < minDistance) {
                    if (distance < 0.1f) {
                        dx = 0.1f;
                        distance = 0.1f;
                    }

                    float nx = dx / distance;
                    float ny = dy / distance;

                    // SNAP TO AXES logic for crates
                    if (pComp.snapToAxes) {
                        if (Math.abs(nx) > Math.abs(ny)) {
                            ny = 0;
                            nx = Math.signum(nx);
                        } else {
                            nx = 0;
                            ny = Math.signum(ny);
                        }
                    }

                    float pushForce = (350f / pComp.mass);
                    tv.vx = nx * pushForce;
                    tv.vy = ny * pushForce;
                    
                    float overlap = minDistance - distance;
                    tt.x += nx * overlap;
                    tt.y += ny * overlap;
                }
            }
        }

        // Apply friction
        for (Entity pushable : pushables) {
            VelocityComponent tv = entityManager.getComponent(pushable, VelocityComponent.class);
            PushableComponent pComp = entityManager.getComponent(pushable, PushableComponent.class);

            float frictionFactor = 1.0f - (pComp.friction * deltaTime);
            if (frictionFactor < 0) frictionFactor = 0;
            
            tv.vx *= frictionFactor;
            tv.vy *= frictionFactor;

            if (Math.abs(tv.vx) < 5f) tv.vx = 0;
            if (Math.abs(tv.vy) < 5f) tv.vy = 0;
        }
    }
}
