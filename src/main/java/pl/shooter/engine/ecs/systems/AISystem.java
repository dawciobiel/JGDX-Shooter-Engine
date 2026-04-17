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
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Manages NPC behaviors, including chasing, shooting and melee attacks.
 * Optimized for better performance and cleaner logic.
 */
public class AISystem extends GameSystem {
    private final EventBus eventBus;
    private final Vector2 tempTarget = new Vector2();
    private GameMap map;
    private float separationUpdateTimer = 0;
    private static final float SEPARATION_UPDATE_INTERVAL = 0.2f; // Update neighbors 5 times per second

    public AISystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    public void setMap(GameMap map) {
        this.map = map;
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

        separationUpdateTimer += deltaTime;
        boolean updateSeparation = separationUpdateTimer >= SEPARATION_UPDATE_INTERVAL;
        if (updateSeparation) separationUpdateTimer = 0;

        for (Entity enemy : enemies) {
            AIComponent ai = entityManager.getComponent(enemy, AIComponent.class);
            TransformComponent enemyTrans = entityManager.getComponent(enemy, TransformComponent.class);
            VelocityComponent enemyVel = entityManager.getComponent(enemy, VelocityComponent.class);
            AnimationComponent anim = entityManager.getComponent(enemy, AnimationComponent.class);
            WeaponComponent weapon = entityManager.getComponent(enemy, WeaponComponent.class);
            
            SteeringComponent sc = getOrAddSteering(enemy, enemyTrans, enemyVel, enemies, ai.speed, updateSeparation);
            sc.setMaxLinearSpeed(ai.speed);

            float distanceToPlayer = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distanceToPlayer < ai.detectRange) {
                // Look at player
                enemyTrans.rotation = MathUtils.atan2(playerTrans.y - enemyTrans.y, playerTrans.x - enemyTrans.x) * MathUtils.radiansToDegrees;
                
                boolean isAttacking = false;
                if (weapon != null) {
                    float attackRange = (weapon.type == WeaponComponent.Type.KNIFE) ? weapon.range + 10f : 400f;
                    if (distanceToPlayer <= attackRange) {
                        if (isLineOfSightClear(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y)) {
                            eventBus.publish(new ShootEvent(enemy, playerTrans.x, playerTrans.y));
                            isAttacking = true;
                        }
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

    private boolean isLineOfSightClear(float x1, float y1, float x2, float y2) {
        if (map == null) return true;
        float dist = Vector2.dst(x1, y1, x2, y2);
        if (dist < 10) return true;
        
        int steps = (int) (dist / 16); // Increased step size for performance
        if (steps < 2) steps = 2;
        
        float dx = (x2 - x1) / steps;
        float dy = (y2 - y1) / steps;
        
        for (int i = 1; i < steps; i++) {
            if (!map.isWalkable(x1 + dx * i, y1 + dy * i)) {
                return false;
            }
        }
        return true;
    }

    private SteeringComponent getOrAddSteering(Entity enemy, TransformComponent t, VelocityComponent v, 
                                              List<Entity> allEnemies, float maxSpeed, boolean updateNeighbors) {
        SteeringComponent sc = entityManager.getComponent(enemy, SteeringComponent.class);
        if (sc == null) {
            sc = new SteeringComponent(t, v);
            sc.setMaxLinearSpeed(maxSpeed);
            sc.setMaxLinearAcceleration(1000f);
            sc.seekBehavior = new Seek<>(sc, new StaticLocation(new Vector2()));
            Array<Steerable<Vector2>> otherAgents = new Array<>();
            sc.separationBehavior = new Separation<>(sc, new RadiusProximity<>(sc, otherAgents, 40f));
            sc.prioritySteering = new PrioritySteering<>(sc);
            sc.prioritySteering.add(sc.separationBehavior);
            sc.prioritySteering.add(sc.seekBehavior);
            sc.behavior = sc.prioritySteering;
            entityManager.addComponent(enemy, sc);
            updateNeighbors = true; // Force update for new component
        }
        
        if (updateNeighbors && sc.separationBehavior != null) {
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
        if (ai.stopToShoot && isAttacking) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
            return;
        }

        if (distanceToPlayer < ai.preferredRange) {
            sc.behavior = null;
            sc.velocity.vx = 0;
            sc.velocity.vy = 0;
            if (anim != null) anim.currentState = AnimationComponent.State.IDLE;
            return;
        }

        if (isAttacking) {
            if (anim != null) anim.currentState = AnimationComponent.State.SHOOT;
        } else {
            if (anim != null) anim.currentState = AnimationComponent.State.WALK;
        }

        if (ai.behavior == AIComponent.Behavior.CHASE) {
            boolean usingPathNode = false;
            if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                if (ai.currentPathIndex >= ai.currentPath.getCount()) ai.currentPathIndex = 1;
                Node targetNode = ai.currentPath.get(ai.currentPathIndex);
                float tx = targetNode.x * 32 + 16;
                float ty = targetNode.y * 32 + 16;

                if (Vector2.dst(sc.transform.x, sc.transform.y, tx, ty) < 6f) {
                    if (ai.currentPathIndex < ai.currentPath.getCount() - 1) {
                        ai.currentPathIndex++;
                    }
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
