package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AnimationComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.ecs.components.WeaponComponent;

import java.util.List;

/**
 * Updates animation timers and manages current animation state (IDLE, WALK, SHOOT).
 * Safely handles missing animations.
 */
public class AnimationSystem extends GameSystem {

    public AnimationSystem(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = entityManager.getEntitiesWithComponents(AnimationComponent.class);

        for (Entity entity : entities) {
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            VelocityComponent velocity = entityManager.getComponent(entity, VelocityComponent.class);

            // 1. Advance the animation timer
            anim.stateTime += deltaTime;

            // 2. State selection
            AnimationComponent.State nextState = AnimationComponent.State.IDLE;

            // Priority: Movement (Walking)
            if (velocity != null) {
                float speedSq = velocity.vx * velocity.vx + velocity.vy * velocity.vy;
                if (speedSq > 0.01f) {
                    nextState = AnimationComponent.State.WALK;
                }
            }
            
            // Priority: Shooting overrides Walking/Idle (if animation exists)
            if (anim.currentState == AnimationComponent.State.SHOOT) {
                var shootAnim = anim.animations.get(AnimationComponent.State.SHOOT);
                if (shootAnim != null && !shootAnim.isAnimationFinished(anim.stateTime)) {
                    nextState = AnimationComponent.State.SHOOT;
                }
            }

            // Safety check: ensure nextState exists in this entity
            if (anim.animations.get(nextState) == null) {
                // If IDLE is missing, fallback to first available animation or stay in current
                if (nextState != AnimationComponent.State.IDLE && anim.animations.get(AnimationComponent.State.IDLE) != null) {
                    nextState = AnimationComponent.State.IDLE;
                } else if (anim.animations.size() > 0) {
                    // Just take any available
                    nextState = anim.animations.keySet().iterator().next();
                }
            }

            // Apply state change and reset timer if state changed
            if (anim.currentState != nextState) {
                anim.currentState = nextState;
                anim.stateTime = 0;
            }
        }
    }
}
