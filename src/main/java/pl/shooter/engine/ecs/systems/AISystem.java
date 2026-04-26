package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Advanced AI System using GDX AI Steering.
 */
public class AISystem extends GameSystem {
    private final EventBus eventBus;
    private GameMap map;

    public AISystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    public void setMap(GameMap map) { this.map = map; }

    @Override
    public void update(float deltaTime) {
        List<Entity> aiEntities = entityManager.getEntitiesWithComponents(AIComponent.class, TransformComponent.class, SteeringComponent.class);
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);

        if (players.isEmpty()) return;
        Entity player = players.getFirst();
        TransformComponent playerTrans = entityManager.getComponent(player, TransformComponent.class);
        
        // We use the player's SteeringComponent (or just a fake location) as target
        // For simplicity, let's treat the player as a static location for the Seek behavior
        SimpleLocation playerLocation = new SimpleLocation(playerTrans.x, playerTrans.y);

        for (Entity entity : aiEntities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            SteeringComponent steering = entityManager.getComponent(entity, SteeringComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);
            
            float dist = Vector2.dst(t.x, t.y, playerTrans.x, playerTrans.y);
            
            if (dist < 800f) {
                // If not already seeking, or target moved, update behavior
                if (steering.seekBehavior == null) {
                    steering.seekBehavior = new Seek<>(steering, playerLocation);
                    steering.behavior = steering.seekBehavior;
                }
                
                // Update simple location target
                playerLocation.set(playerTrans.x, playerTrans.y);
                
                // Rotate towards player
                t.rotation = (float) Math.toDegrees(Math.atan2(playerTrans.y - t.y, playerTrans.x - t.x));
                
                // Shooting logic
                if (dist < 350f) {
                    eventBus.publish(new ShootEvent(entity, playerTrans.x, playerTrans.y));
                }
            } else {
                steering.behavior = null;
                v.vx = 0;
                v.vy = 0;
            }
        }
    }

    // Inner class for simple target location
    private static class SimpleLocation implements com.badlogic.gdx.ai.utils.Location<Vector2> {
        private final Vector2 pos = new Vector2();
        public SimpleLocation(float x, float y) { pos.set(x, y); }
        public void set(float x, float y) { pos.set(x, y); }
        @Override public Vector2 getPosition() { return pos; }
        @Override public float getOrientation() { return 0; }
        @Override public void setOrientation(float orientation) {}
        @Override public float vectorToAngle(Vector2 vector) { return 0; }
        @Override public Vector2 angleToVector(Vector2 outVector, float angle) { return outVector; }
        @Override public com.badlogic.gdx.ai.utils.Location<Vector2> newLocation() { return null; }
    }
}
