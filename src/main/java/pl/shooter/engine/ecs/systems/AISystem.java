package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Controls AI-driven entities with animation state awareness, shooting and melee attacks.
 */
public class AISystem extends GameSystem {
    private final EventBus eventBus;
    private float meleeTimer = 0;

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

            float distance = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distance < ai.detectRange) {
                // Look at player
                Vector2 direction = new Vector2(playerTrans.x - enemyTrans.x, playerTrans.y - enemyTrans.y);
                direction.nor();
                enemyTrans.rotation = direction.angleDeg();

                // 1. Shooting (if has weapon and in range)
                if (weapon != null) {
                    eventBus.publish(new ShootEvent(enemy, playerTrans.x, playerTrans.y));
                }

                // 2. Movement and Melee
                if (distance < 65f) {
                    // Close range: Stop and attack
                    enemyVel.vx = 0;
                    enemyVel.vy = 0;
                    
                    if (anim != null && anim.currentState != AnimationComponent.State.SHOOT) {
                        anim.currentState = AnimationComponent.State.SHOOT;
                        anim.stateTime = 0;
                    }
                    
                    // Periodic melee damage
                    if (meleeTimer > 1.0f) { 
                        eventBus.publish(new HitEvent(enemy, player));
                        meleeTimer = 0;
                    }
                } else if (ai.behavior == AIComponent.Behavior.CHASE) {
                    // Far range: Chase player
                    enemyVel.vx = direction.x * 100f;
                    enemyVel.vy = direction.y * 100f;
                    
                    if (anim != null && anim.currentState == AnimationComponent.State.SHOOT) {
                        anim.currentState = AnimationComponent.State.WALK;
                        anim.stateTime = 0;
                    }
                }
            } else {
                // Player out of detection range
                enemyVel.vx = 0;
                enemyVel.vy = 0;
                if (anim != null && anim.currentState != AnimationComponent.State.IDLE) {
                    anim.currentState = AnimationComponent.State.IDLE;
                    anim.stateTime = 0;
                }
            }
        }
    }
}
