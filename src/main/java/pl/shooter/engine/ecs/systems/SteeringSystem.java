package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SteeringComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Updates entity velocity based on Steering Behaviors and logs to file.
 */
public class SteeringSystem extends GameSystem {
    private float logTimer = 0;
    private static final String LOG_FILE = "ai_debug.log";
    
    public SteeringSystem(EntityManager entityManager) {
        super(entityManager);
        // Clear log file on startup
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, false))) {
            out.println("--- AI DEBUG START ---");
        } catch (Exception ignored) {}
    }

    @Override
    public void update(float deltaTime) {
        logTimer += deltaTime;
        boolean shouldLog = logTimer >= 0.5f;
        if (shouldLog) logTimer = 0;

        List<Entity> steerables = entityManager.getEntitiesWithComponents(SteeringComponent.class, VelocityComponent.class);
        
        for (Entity entity : steerables) {
            SteeringComponent sc = entityManager.getComponent(entity, SteeringComponent.class);
            VelocityComponent vc = entityManager.getComponent(entity, VelocityComponent.class);

            String status = "STATIONARY";
            String targetStr = "NONE";
            float dist = 0;

            if (sc.behavior != null && sc.behavior instanceof Seek<Vector2> seek) {
                Vector2 target = seek.getTarget().getPosition();
                targetStr = String.format("(%.1f, %.1f)", target.x, target.y);
                Vector2 current = sc.getPosition();
                
                float dx = target.x - current.x;
                float dy = target.y - current.y;
                dist = (float)Math.sqrt(dx*dx + dy*dy);

                if (dist > 5f) { 
                    vc.vx = (dx / dist) * sc.maxLinearSpeed;
                    vc.vy = (dy / dist) * sc.maxLinearSpeed;
                    status = "MOVING";
                } else {
                    vc.vx = 0;
                    vc.vy = 0;
                    status = "REACHED_TARGET";
                }
            } else {
                vc.vx = 0;
                vc.vy = 0;
                status = (sc.behavior == null) ? "BEHAVIOR_NULL" : "UNKNOWN_BEHAVIOR";
            }

            if (shouldLog) {
                logToFile(String.format(
                    "T: %.2f | ID: %d | Status: %s | Pos: (%.1f, %.1f) | Target: %s | Dist: %.1f | Vel: (%.1f, %.1f)",
                    Gdx.graphics.getDeltaTime(), entity.getId(), status, sc.transform.x, sc.transform.y, targetStr, dist, vc.vx, vc.vy
                ));
            }
        }
    }

    private void logToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(message);
        } catch (Exception ignored) {}
    }
}
