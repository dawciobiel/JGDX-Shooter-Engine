package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;

import java.util.List;

public class AISystem extends GameSystem {
    private final EventBus eventBus;
    private static final float MELEE_RANGE = 45f;
    private final Vector2 tempTarget = new Vector2();

    public AISystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;
        
        Entity player = players.get(0);
        TransformComponent playerTrans = entityManager.getComponent(player, TransformComponent.class);

        List<Entity> enemies = entityManager.getEntitiesWithComponents(
                AIComponent.class, TransformComponent.class, VelocityComponent.class
        );

        for (Entity enemy : enemies) {
            AIComponent ai = entityManager.getComponent(enemy, AIComponent.class);
            TransformComponent enemyTrans = entityManager.getComponent(enemy, TransformComponent.class);
            VelocityComponent enemyVel = entityManager.getComponent(enemy, VelocityComponent.class);
            AnimationComponent anim = entityManager.getComponent(enemy, AnimationComponent.class);
            
            SteeringComponent sc = getOrAddSteering(enemy, enemyTrans, enemyVel);
            float distanceToPlayer = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distanceToPlayer < ai.detectRange) {
                // Point towards player
                enemyTrans.rotation = MathUtils.atan2(playerTrans.y - enemyTrans.y, playerTrans.x - enemyTrans.x) * MathUtils.radiansToDegrees;
                
                handleLogic(enemy, ai, sc, playerTrans, anim, distanceToPlayer);
            } else {
                sc.behavior = null;
                enemyVel.vx = 0;
                enemyVel.vy = 0;
                if (anim != null) anim.currentState = AnimationComponent.State.IDLE;
            }
        }
    }

    private SteeringComponent getOrAddSteering(Entity enemy, TransformComponent t, VelocityComponent v) {
        SteeringComponent sc = entityManager.getComponent(enemy, SteeringComponent.class);
        if (sc == null) {
            sc = new SteeringComponent(t, v);
            sc.setMaxLinearSpeed(70f);
            sc.setMaxLinearAcceleration(1000f);
            sc.seekBehavior = new Seek<>(sc, new StaticLocation(new Vector2()));
            sc.behavior = sc.seekBehavior;
            entityManager.addComponent(enemy, sc);
        }
        return sc;
    }

    private void handleLogic(Entity enemy, AIComponent ai, SteeringComponent sc,
                             TransformComponent playerTrans, AnimationComponent anim, float distanceToPlayer) {
        
        if (distanceToPlayer < MELEE_RANGE) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
            return;
        }

        if (ai.behavior == AIComponent.Behavior.CHASE) {
            boolean usingPathNode = false;
            
            // 1. If very close to player, skip pathfinding logic and go direct
            if (distanceToPlayer < 80f) {
                tempTarget.set(playerTrans.x, playerTrans.y);
            } 
            // 2. Use pathfinding nodes if available
            else if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                if (ai.currentPathIndex >= ai.currentPath.getCount()) ai.currentPathIndex = 1;

                Node targetNode = ai.currentPath.get(ai.currentPathIndex);
                float tx = targetNode.x * 32 + 16;
                float ty = targetNode.y * 32 + 16;

                // Advance to next node if close enough (reduced threshold to 8px)
                if (Vector2.dst(sc.transform.x, sc.transform.y, tx, ty) < 8f) {
                    if (ai.currentPathIndex < ai.currentPath.getCount() - 1) {
                        ai.currentPathIndex++;
                        targetNode = ai.currentPath.get(ai.currentPathIndex);
                        tx = targetNode.x * 32 + 16;
                        ty = targetNode.y * 32 + 16;
                    }
                }
                tempTarget.set(tx, ty);
                usingPathNode = true;
            }

            if (!usingPathNode && distanceToPlayer >= 80f) {
                tempTarget.set(playerTrans.x, playerTrans.y);
            }

            // Always update seeker target
            ((StaticLocation)sc.seekBehavior.getTarget()).pos.set(tempTarget);
            sc.behavior = sc.seekBehavior;

            if (anim != null && anim.currentState != AnimationComponent.State.WALK) {
                anim.currentState = AnimationComponent.State.WALK;
            }
        }
    }

    private static class StaticLocation implements com.badlogic.gdx.ai.utils.Location<Vector2> {
        public final Vector2 pos;
        public StaticLocation(Vector2 pos) { this.pos = pos; }
        @Override public Vector2 getPosition() { return pos; }
        @Override public float getOrientation() { return 0; }
        @Override public void setOrientation(float orientation) {}
        @Override public float vectorToAngle(Vector2 vector) { return MathUtils.atan2(vector.y, vector.x); }
        @Override public Vector2 angleToVector(Vector2 outVector, float angle) {
            outVector.x = MathUtils.cos(angle);
            outVector.y = MathUtils.sin(angle);
            return outVector;
        }
        @Override public com.badlogic.gdx.ai.utils.Location<Vector2> newLocation() { return new StaticLocation(new Vector2()); }
    }
}
