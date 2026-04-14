package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ScoreEvent;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Handles health reduction, death, scoring, and item drops.
 */
public class DamageSystem extends GameSystem {
    private final EntityFactory factory;
    private final EventBus eventBus;

    public DamageSystem(EntityManager entityManager, EventBus eventBus, EntityFactory factory) {
        super(entityManager);
        this.eventBus = eventBus;
        this.factory = factory;
        eventBus.subscribe(HitEvent.class, this::handleHit);
    }

    @Override
    public void update(float deltaTime) {}

    private void handleHit(HitEvent event) {
        ProjectileComponent bullet = entityManager.getComponent(event.attacker, ProjectileComponent.class);
        
        boolean isVictimPlayer = entityManager.hasComponent(event.victim, PlayerComponent.class);
        boolean isVictimDestructible = entityManager.hasComponent(event.victim, DestructibleComponent.class);
        boolean isAttackerPlayerBullet = false;
        
        if (bullet != null) {
            Entity owner = entityManager.getEntityById(bullet.ownerId);
            if (owner != null && entityManager.hasComponent(owner, PlayerComponent.class)) {
                isAttackerPlayerBullet = true;
            }
        } else {
            if (entityManager.hasComponent(event.attacker, PlayerComponent.class)) {
                isAttackerPlayerBullet = true;
            }
        }

        // --- NEW LOGIC: Friendly Fire - Bullet passes through friends ---
        boolean shouldDamage = false;
        if (isVictimPlayer && !isAttackerPlayerBullet) {
            shouldDamage = true; // Monster hits player
        } else if (!isVictimPlayer && isAttackerPlayerBullet) {
            shouldDamage = true; // Player hits monster or scenery
        }

        if (!shouldDamage) {
            // Bullet just flies through! Do not remove bullet, do not apply damage.
            return;
        }

        // If we reach here, we are doing damage. Remove bullet.
        if (bullet != null) {
            entityManager.removeEntity(event.attacker);
        }

        HealthComponent health = entityManager.getComponent(event.victim, HealthComponent.class);
        TransformComponent trans = entityManager.getComponent(event.victim, TransformComponent.class);

        if (health != null && trans != null) {
            health.hp -= 10;
            
            if (isVictimDestructible) {
                factory.createExplosion(trans.x, trans.y, new Color(0.6f, 0.4f, 0.2f, 1f));
            } else {
                factory.createExplosion(trans.x, trans.y, Color.RED);
            }

            if (health.hp <= 0) {
                onEntityDeath(event.victim, trans.x, trans.y, isAttackerPlayerBullet, isVictimDestructible);
            }
        }
    }

    private void onEntityDeath(Entity victim, float x, float y, boolean killedByPlayer, boolean isDestructible) {
        if (isDestructible) {
            factory.createExplosion(x, y, new Color(0.4f, 0.2f, 0.1f, 1f));
            entityManager.removeEntity(victim);
            return;
        }

        if (entityManager.hasComponent(victim, PlayerComponent.class)) {
            factory.createExplosion(x, y, Color.RED);
            entityManager.removeEntity(victim);
        } else {
            if (killedByPlayer) {
                List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
                if (!players.isEmpty()) {
                    ScoreComponent score = entityManager.getComponent(players.get(0), ScoreComponent.class);
                    score.score += 100;
                    score.kills += 1;
                }
                eventBus.publish(new ScoreEvent(100));
                
                float roll = MathUtils.random();
                if (roll < 0.20f) {
                    factory.createAmmoPickup(x, y, MathUtils.random(5, 15));
                } else if (roll < 0.10f) {
                    factory.createHealthPickup(x, y, 20f);
                }
            }
            entityManager.removeEntity(victim);
        }
    }
}
