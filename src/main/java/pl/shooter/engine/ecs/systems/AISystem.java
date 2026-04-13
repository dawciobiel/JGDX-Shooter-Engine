package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.events.HitEvent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class AISystem extends GameSystem {
    private final EventBus eventBus;
    private float meleeTimer = 0;
    private static final float MELEE_RANGE = 28f;

    public AISystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        meleeTimer += deltaTime;
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
            WeaponComponent weapon = entityManager.getComponent(enemy, WeaponComponent.class);
            
            SteeringComponent sc = getOrAddSteering(enemy, enemyTrans, enemyVel);
            float distanceToPlayer = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distanceToPlayer < ai.detectRange) {
                // Look at player (Fixed orientation)
                enemyTrans.rotation = MathUtils.atan2(playerTrans.y - enemyTrans.y, playerTrans.x - enemyTrans.x) * MathUtils.radiansToDegrees;
                
                // Shoot
                if (weapon != null) {
                    eventBus.publish(new ShootEvent(enemy, playerTrans.x, playerTrans.y));
                }

                handleLogic(enemy, ai, sc, enemies, playerTrans, player, anim, distanceToPlayer);
            } else {
                sc.behavior = null;
                enemyVel.vx = 0;
                enemyVel.vy = 0;
                if (anim != null && anim.currentState != AnimationComponent.State.IDLE) {
                    anim.currentState = AnimationComponent.State.IDLE;
                }
            }
        }
    }

    private SteeringComponent getOrAddSteering(Entity enemy, TransformComponent t, VelocityComponent v) {
        SteeringComponent sc = entityManager.getComponent(enemy, SteeringComponent.class);
        if (sc == null) {
            sc = new SteeringComponent(t, v);
            sc.setMaxLinearSpeed(60f); // Slow zombies
            sc.setMaxLinearAcceleration(400f);
            entityManager.addComponent(enemy, sc);
        }
        return sc;
    }

    private void handleLogic(Entity enemy, AIComponent ai, SteeringComponent sc,
                             List<Entity> allEnemies, TransformComponent playerTrans, 
                             Entity player, AnimationComponent anim, float distanceToPlayer) {
        
        if (distanceToPlayer < MELEE_RANGE) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
            if (meleeTimer > 1.0f) { 
                eventBus.publish(new HitEvent(enemy, player));
                meleeTimer = 0;
            }
            return;
        }

        if (ai.behavior == AIComponent.Behavior.CHASE) {
            Vector2 targetPos = null;
            
            // Follow path node if exists
            if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                Node nextNode = ai.currentPath.get(1);
                targetPos = new Vector2(nextNode.x * 32 + 16, nextNode.y * 32 + 16);
                
                // If close to next node, aim for the one after
                if (Vector2.dst(sc.transform.x, sc.transform.y, targetPos.x, targetPos.y) < 15f && ai.currentPath.getCount() > 2) {
                    Node futureNode = ai.currentPath.get(2);
                    targetPos.set(futureNode.x * 32 + 16, futureNode.y * 32 + 16);
                }
            } else if (distanceToPlayer < 100f) {
                // Fallback: direct line if very close (e.g. within same tile)
                targetPos = new Vector2(playerTrans.x, playerTrans.y);
            }

            if (targetPos != null) {
                Arrive<Vector2> arrive = new Arrive<>(sc, new StaticLocation(targetPos))
                        .setArrivalTolerance(5f)
                        .setDecelerationRadius(20f);

                Array<Steerable<Vector2>> otherEnemies = new Array<>();
                for (Entity e : allEnemies) {
                    if (e == enemy) continue;
                    SteeringComponent otherSc = entityManager.getComponent(e, SteeringComponent.class);
                    if (otherSc != null) otherEnemies.add(otherSc);
                }
                
                Separation<Vector2> separation = new Separation<>(sc, new RadiusProximity<>(sc, otherEnemies, 25f));

                PrioritySteering<Vector2> priority = new PrioritySteering<>(sc);
                priority.add(separation);
                priority.add(arrive);
                
                sc.behavior = priority;

                if (anim != null && anim.currentState == AnimationComponent.State.SHOOT) {
                    anim.currentState = AnimationComponent.State.WALK;
                }
            } else {
                // Stop if no path found
                sc.behavior = null;
                sc.velocity.vx = 0;
                sc.velocity.vy = 0;
                if (anim != null) anim.currentState = AnimationComponent.State.IDLE;
            }
        }
    }

    private static class StaticLocation implements com.badlogic.gdx.ai.utils.Location<Vector2> {
        private final Vector2 pos;
        public StaticLocation(Vector2 pos) { this.pos = pos; }
        @Override public Vector2 getPosition() { return pos; }
        @Override public float getOrientation() { return 0; }
        @Override public void setOrientation(float orientation) {}
        @Override public float vectorToAngle(Vector2 vector) { return 0; }
        @Override public Vector2 angleToVector(Vector2 outVector, float angle) { return outVector; }
        @Override public com.badlogic.gdx.ai.utils.Location<Vector2> newLocation() { return null; }
    }
}
