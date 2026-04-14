package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
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

import java.util.List;

/**
 * Manages NPC behaviors, including chasing, shooting and melee attacks.
 * Now respects weapon range for all attack types.
 */
public class AISystem extends GameSystem {
    private final EventBus eventBus;
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
            WeaponComponent weapon = entityManager.getComponent(enemy, WeaponComponent.class);
            
            SteeringComponent sc = getOrAddSteering(enemy, enemyTrans, enemyVel, enemies);
            float distanceToPlayer = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distanceToPlayer < ai.detectRange) {
                // Point towards player
                enemyTrans.rotation = MathUtils.atan2(playerTrans.y - enemyTrans.y, playerTrans.x - enemyTrans.x) * MathUtils.radiansToDegrees;
                
                // Tactics: Shooting/Melee logic based on weapon range
                boolean isAttacking = false;
                if (weapon != null) {
                    float attackRange = (weapon.type == WeaponComponent.Type.KNIFE) ? weapon.range + 10f : 400f; // Default 400 for firearms
                    if (distanceToPlayer <= attackRange) {
                        eventBus.publish(new ShootEvent(enemy, playerTrans.x, playerTrans.y));
                        isAttacking = true;
                    }
                }

                handleTacticalMovement(enemy, ai, sc, playerTrans, anim, distanceToPlayer, isAttacking);
            } else {
                sc.behavior = null;
                enemyVel.vx = 0;
                enemyVel.vy = 0;
                if (anim != null) anim.currentState = AnimationComponent.State.IDLE;
            }
        }
    }

    private SteeringComponent getOrAddSteering(Entity enemy, TransformComponent t, VelocityComponent v, List<Entity> allEnemies) {
        SteeringComponent sc = entityManager.getComponent(enemy, SteeringComponent.class);
        if (sc == null) {
            sc = new SteeringComponent(t, v);
            sc.setMaxLinearSpeed(75f);
            sc.setMaxLinearAcceleration(1000f);
            sc.seekBehavior = new Seek<>(sc, new StaticLocation(new Vector2()));
            Array<Steerable<Vector2>> otherAgents = new Array<>();
            sc.separationBehavior = new Separation<>(sc, new RadiusProximity<>(sc, otherAgents, 40f));
            sc.prioritySteering = new PrioritySteering<>(sc);
            sc.prioritySteering.add(sc.separationBehavior);
            sc.prioritySteering.add(sc.seekBehavior);
            sc.behavior = sc.prioritySteering;
            entityManager.addComponent(enemy, sc);
        }
        
        if (sc.separationBehavior != null) {
            Array<Steerable<Vector2>> agents = (Array<Steerable<Vector2>>) ((RadiusProximity<Vector2>)sc.separationBehavior.getProximity()).getAgents();
            agents.clear();
            for (Entity e : allEnemies) {
                if (e == enemy) continue;
                SteeringComponent otherSc = entityManager.getComponent(e, SteeringComponent.class);
                if (otherSc != null) agents.add(otherSc);
            }
        }
        return sc;
    }

    private void handleTacticalMovement(Entity enemy, AIComponent ai, SteeringComponent sc,
                                        TransformComponent playerTrans, AnimationComponent anim, 
                                        float distanceToPlayer, boolean isAttacking) {
        
        // 1. Should we stop because we are attacking?
        if (ai.stopToShoot && isAttacking) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
            return;
        }

        // 2. Should we stop because we reached preferred distance?
        if (distanceToPlayer < ai.preferredRange) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.IDLE;
            return;
        }

        // 3. Animation trigger based on attack state
        if (isAttacking) {
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
        } else {
            if (anim != null) anim.currentState = AnimationComponent.State.WALK;
        }

        // 4. Normal Pathfinding / Chase
        if (ai.behavior == AIComponent.Behavior.CHASE) {
            boolean usingPathNode = false;
            if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                if (ai.currentPathIndex >= ai.currentPath.getCount()) ai.currentPathIndex = 1;
                Node targetNode = ai.currentPath.get(ai.currentPathIndex);
                float tx = targetNode.x * 32 + 16;
                float ty = targetNode.y * 32 + 16;

                if (Vector2.dst(sc.transform.x, sc.transform.y, tx, ty) < 12f) {
                    if (ai.currentPathIndex < ai.currentPath.getCount() - 1) ai.currentPathIndex++;
                }
                tempTarget.set(tx, ty);
                usingPathNode = true;
            }

            if (!usingPathNode) tempTarget.set(playerTrans.x, playerTrans.y);

            ((StaticLocation)sc.seekBehavior.getTarget()).pos.set(tempTarget);
            sc.behavior = sc.prioritySteering;
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
