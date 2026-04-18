package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.DeathEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ScoreEvent;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Handles health reduction, death, scoring, item drops, and corpse/blood system.
 */
public class DamageSystem extends GameSystem {
    private final EntityFactory factory;
    private final EventBus eventBus;
    private final GameConfig config;

    public DamageSystem(EntityManager entityManager, EventBus eventBus, EntityFactory factory, GameConfig config) {
        super(entityManager);
        this.eventBus = eventBus;
        this.factory = factory;
        this.config = config;
        eventBus.subscribe(HitEvent.class, this::handleHit);
    }

    @Override
    public void update(float deltaTime) {
        // Handle corpse fading and removal
        List<Entity> healthEntities = entityManager.getEntitiesWithComponents(HealthComponent.class);
        for (Entity entity : healthEntities) {
            HealthComponent health = entityManager.getComponent(entity, HealthComponent.class);
            if (health.isDead) {
                health.deathTimer += deltaTime;
                if (health.deathTimer >= health.corpseDuration) {
                    entityManager.removeEntity(entity);
                }
            }
        }
    }

    private void handleHit(HitEvent event) {
        Entity victim = event.victim;
        HealthComponent health = entityManager.getComponent(victim, HealthComponent.class);
        
        // Don't hit things that are already dead
        if (health == null || health.isDead) return;

        boolean isVictimPlayer = entityManager.hasComponent(victim, PlayerComponent.class);
        
        // DEBUG: Invincibility
        if (isVictimPlayer && config.debug.invinciblePlayer) return;

        int attackerId = event.attackerId;
        int damage = event.damage;

        boolean isVictimDestructible = entityManager.hasComponent(victim, DestructibleComponent.class);
        
        Entity attacker = entityManager.getEntityById(attackerId);
        boolean isAttackerPlayer = attacker != null && entityManager.hasComponent(attacker, PlayerComponent.class);

        // --- Friendly Fire Logic ---
        boolean shouldDamage = false;
        if (isVictimPlayer && !isAttackerPlayer) {
            shouldDamage = true; // Monster/Explosion hits player
        } else if (!isVictimPlayer && isAttackerPlayer) {
            shouldDamage = true; // Player hits monster or scenery
        }

        if (!shouldDamage) return;

        TransformComponent trans = entityManager.getComponent(victim, TransformComponent.class);

        if (trans != null) {
            health.hp -= damage;
            
            // Effects of hit
            if (isVictimDestructible) {
                factory.createExplosion(trans.x, trans.y, new Color(0.6f, 0.4f, 0.2f, 1f));
            } else if (health.hasBlood) {
                factory.createExplosion(trans.x, trans.y, health.bloodColor); // Splatter effect
            } else {
                factory.createExplosion(trans.x, trans.y, Color.GRAY); // Sparks/Dust
            }

            if (health.hp <= 0) {
                onEntityDeath(victim, trans.x, trans.y, isAttackerPlayer, isVictimDestructible);
            }
        }
    }

    private void onEntityDeath(Entity victim, float x, float y, boolean killedByPlayer, boolean isDestructible) {
        HealthComponent health = entityManager.getComponent(victim, HealthComponent.class);
        
        if (isDestructible) {
            factory.createExplosion(x, y, new Color(0.4f, 0.2f, 0.1f, 1f));
            entityManager.removeEntity(victim);
            return;
        }

        eventBus.publish(new DeathEvent(victim));

        if (entityManager.hasComponent(victim, PlayerComponent.class)) {
            factory.createExplosion(x, y, Color.RED);
            health.isDead = true; // Mark player dead, but don't remove yet for death screen/fade
        } else {
            if (killedByPlayer) {
                List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
                if (!players.isEmpty()) {
                    ScoreComponent score = entityManager.getComponent(players.getFirst(), ScoreComponent.class);
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
            
            // CORPSE SYSTEM: Start death timer and disable AI/Collisions
            health.isDead = true;
            entityManager.removeComponent(victim, AIComponent.class);
            entityManager.removeComponent(victim, ColliderComponent.class);
            entityManager.removeComponent(victim, VelocityComponent.class);
            entityManager.removeComponent(victim, SteeringComponent.class);
        }
    }
}
