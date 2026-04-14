package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SteeringComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;

import java.util.List;

/**
 * Updates entity velocity based on Steering Behaviors.
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
                // Use gdx-ai to calculate the steering output
                sc.getSteeringOutput().setZero();
                sc.behavior.calculateSteering(sc.getSteeringOutput());
                
                // Kinematic update: assign linear acceleration directly to velocity
                // This makes the movement very responsive
                float lx = sc.getSteeringOutput().linear.x;
                float ly = sc.getSteeringOutput().linear.y;

                if (lx != 0 || ly != 0) {
                    vc.vx = lx;
                    vc.vy = ly;
                    
                    // Manually limit speed
                    float speedSq = vc.vx * vc.vx + vc.vy * vc.vy;
                    if (speedSq > sc.maxLinearSpeed * sc.maxLinearSpeed) {
                        float speed = (float)Math.sqrt(speedSq);
                        vc.vx = (vc.vx / speed) * sc.maxLinearSpeed;
                        vc.vy = (vc.vy / speed) * sc.maxLinearSpeed;
                    }
                } else {
                    // Gradual slow down if no steering
                    vc.vx *= 0.1f;
                    vc.vy *= 0.1f;
                }
            }
        }
    }
}
