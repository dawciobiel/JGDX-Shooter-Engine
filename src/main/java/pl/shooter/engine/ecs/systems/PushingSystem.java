package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Handles physical interaction (pushing) between all active units and pushable objects.
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
        
        // All units that can move (Player + Enemies)
        List<Entity> pushers = entityManager.getEntitiesWithComponents(
                TransformComponent.class, VelocityComponent.class, ColliderComponent.class);

        if (pushables.isEmpty() || pushers.isEmpty()) return;

        for (Entity pusher : pushers) {
            TransformComponent pt = entityManager.getComponent(pusher, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(pusher, ColliderComponent.class);
            VelocityComponent pv = entityManager.getComponent(pusher, VelocityComponent.class);

            for (Entity pushable : pushables) {
                if (pusher.getId() == pushable.getId()) continue;

                PushableComponent pComp = entityManager.getComponent(pushable, PushableComponent.class);
                
                // Restriction check
                if (pComp.playerOnly && !entityManager.hasComponent(pusher, PlayerComponent.class)) continue;

                TransformComponent tt = entityManager.getComponent(pushable, TransformComponent.class);
                ColliderComponent tc = entityManager.getComponent(pushable, ColliderComponent.class);
                VelocityComponent tv = entityManager.getComponent(pushable, VelocityComponent.class);

                float dx = tt.x - pt.x;
                float dy = tt.y - pt.y;
                float minDist = pc.radius + tc.radius;
                float distSq = dx * dx + dy * dy;

                if (distSq < minDist * minDist) {
                    float distance = (float) Math.sqrt(distSq);
                    if (distance < 0.1f) { dx = 0.1f; distance = 0.1f; }

                    float nx = dx / distance;
                    float ny = dy / distance;

                    // SNAP TO AXES (logic for crates)
                    if (pComp.snapToAxes) {
                        if (Math.abs(nx) > Math.abs(ny)) { ny = 0; nx = Math.signum(nx); }
                        else { nx = 0; ny = Math.signum(ny); }
                    }

                    // Push force depends on mass and pusher velocity/strength
                    float pusherSpeed = (float) Math.sqrt(pv.vx * pv.vx + pv.vy * pv.vy);
                    float baseForce = (pusherSpeed > 10) ? 350f : 150f;
                    float pushForce = baseForce / pComp.mass;
                    
                    tv.vx = nx * pushForce;
                    tv.vy = ny * pushForce;
                    
                    // Separation (prevent overlap)
                    float overlap = minDist - distance;
                    tt.x += nx * overlap;
                    tt.y += ny * overlap;
                }
            }
        }

        // Apply friction to all pushable objects
        for (Entity pushable : pushables) {
            VelocityComponent tv = entityManager.getComponent(pushable, VelocityComponent.class);
            PushableComponent pComp = entityManager.getComponent(pushable, PushableComponent.class);

            float frictionFactor = 1.0f - (pComp.friction * deltaTime);
            if (frictionFactor < 0) frictionFactor = 0;
            
            tv.vx *= frictionFactor;
            tv.vy *= frictionFactor;

            if (Math.abs(tv.vx) < 2f) tv.vx = 0;
            if (Math.abs(tv.vy) < 2f) tv.vy = 0;
        }
    }
}
