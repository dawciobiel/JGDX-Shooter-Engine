package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SteeringComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;

import java.util.List;

/**
 * Updates entity velocity based on gdx-ai Steering Behaviors.
 */
public class SteeringSystem extends GameSystem {
    
    public SteeringSystem(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> steerables = entityManager.getEntitiesWithComponents(SteeringComponent.class, VelocityComponent.class);
        
        for (Entity entity : steerables) {
            SteeringComponent sc = entityManager.getComponent(entity, SteeringComponent.class);
            VelocityComponent vc = entityManager.getComponent(entity, VelocityComponent.class);

            if (sc.behavior != null) {
                // Calculate steering acceleration
                sc.behavior.calculateSteering(sc.getSteeringOutput());
                
                // Apply acceleration to velocity
                vc.vx += sc.getSteeringOutput().linear.x * deltaTime;
                vc.vy += sc.getSteeringOutput().linear.y * deltaTime;
                
                // Limit velocity
                float speedSq = vc.vx * vc.vx + vc.vy * vc.vy;
                if (speedSq > sc.maxLinearSpeed * sc.maxLinearSpeed) {
                    float speed = (float)Math.sqrt(speedSq);
                    vc.vx = (vc.vx / speed) * sc.maxLinearSpeed;
                    vc.vy = (vc.vy / speed) * sc.maxLinearSpeed;
                }
            }
        }
    }
}
