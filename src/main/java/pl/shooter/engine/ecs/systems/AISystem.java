package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.behaviors.FollowPath;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.utils.paths.LinePath;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.engine.world.GameMap;
import pl.shooter.events.HitEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced AI System using GDX AI Steering and Pathfinding.
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
        List<Entity> allEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class);
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);

        if (players.isEmpty()) return;
        Entity player = players.getFirst();
        TransformComponent playerTrans = entityManager.getComponent(player, TransformComponent.class);

        for (Entity entity : aiEntities) {
            AIComponent ai = entityManager.getComponent(entity, AIComponent.class);
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            SteeringComponent steering = entityManager.getComponent(entity, SteeringComponent.class);
            VelocityComponent v = entityManager.getComponent(entity, VelocityComponent.class);
            
            float dist = Vector2.dst(t.x, t.y, playerTrans.x, playerTrans.y);
            
            if (ai.behavior == AIComponent.Behavior.CHASE && dist < 800f) {
                if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                    com.badlogic.gdx.utils.Array<Vector2> waypoints = new com.badlogic.gdx.utils.Array<>();
                    for (Node node : ai.currentPath) waypoints.add(new Vector2(node.x * 32 + 16, node.y * 32 + 16));
                    LinePath<Vector2> path = new LinePath<Vector2>(waypoints, false);
                    steering.behavior = new FollowPath<>(steering, path, 20f, 50f);
                } else {
                    steering.behavior = new com.badlogic.gdx.ai.steer.behaviors.Seek<>(steering, new SimpleLocation(playerTrans.x, playerTrans.y));
                }
                
                // Rotate towards player
                t.rotation = (float) Math.toDegrees(Math.atan2(playerTrans.y - t.y, playerTrans.x - t.x));
                
                // Shooting/Melee logic
                float currentTime = com.badlogic.gdx.Gdx.graphics.getDeltaTime(); // Simplification: use a system timer if possible
                // Actually need to track total time
                if (dist < ai.attackRange) { 
                    // Melee range
                    if (ai.lastAttackTime == 0 || (com.badlogic.gdx.utils.TimeUtils.nanoTime() - ai.lastAttackTime) / 1000000000.0 >= ai.attackRate) {
                        eventBus.publish(new HitEvent(player, entity.getId(), ai.attackDamage));
                        ai.lastAttackTime = com.badlogic.gdx.utils.TimeUtils.nanoTime();
                    }
                } else if (dist < 350f) {
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
