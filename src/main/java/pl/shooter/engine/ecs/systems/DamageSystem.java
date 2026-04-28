package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.GameplayConfig;
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
    private final GameplayConfig config;
    private final ConfigService configService;

    public DamageSystem(EntityManager entityManager, EventBus eventBus, EntityFactory factory, GameplayConfig config, ConfigService configService) {
        super(entityManager);
        this.eventBus = eventBus;
        this.factory = factory;
        this.config = config;
        this.configService = configService;
        eventBus.subscribe(HitEvent.class, this::handleHit);
    }

    @Override
    public void update(float deltaTime) {
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
        
        if (health == null || health.isDead) return;

        // Check for invincibility (God Mode)
        if (isInvincible(victim)) return;

        boolean isVictimPlayer = entityManager.hasComponent(victim, PlayerComponent.class);
        
        // Debug/GodMode can be moved to PlayerConfig later if needed
        int attackerId = event.attackerId;
        int damage = event.damage;

        boolean isVictimDestructible = entityManager.hasComponent(victim, DestructibleComponent.class);
        
        Entity attacker = entityManager.getEntityById(attackerId);
        boolean isAttackerPlayer = attacker != null && entityManager.hasComponent(attacker, PlayerComponent.class);

        boolean shouldDamage = false;
        if (isVictimPlayer && !isAttackerPlayer) {
            shouldDamage = true;
        } else if (!isVictimPlayer && isAttackerPlayer) {
            shouldDamage = true;
        }

        if (!shouldDamage) return;

        TransformComponent trans = entityManager.getComponent(victim, TransformComponent.class);

        if (trans != null) {
            health.hp -= damage;
            
            if (isVictimDestructible) {
                factory.createExplosion(trans.x, trans.y, new Color(0.6f, 0.4f, 0.2f, 1f));
            } else if (health.hasBlood) {
                factory.createExplosion(trans.x, trans.y, health.bloodColor);
            } else {
                factory.createExplosion(trans.x, trans.y, Color.GRAY);
            }

            if (health.hp <= 0) {
                onEntityDeath(victim, trans.x, trans.y, isAttackerPlayer, isVictimDestructible);
            }
        }
    }

    private boolean isInvincible(Entity entity) {
        // Global debug flag from EngineConfig
        if (configService != null && configService.getEngineConfig().debug.invinciblePlayer) {
            if (entityManager.hasComponent(entity, PlayerComponent.class)) return true;
        }

        // Entity specific flag (from PlayerConfig)
        PlayerComponent pc = entityManager.getComponent(entity, PlayerComponent.class);
        return pc != null && pc.invincible;
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
            health.isDead = true;
        } else {
            if (killedByPlayer) {
                List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
                if (!players.isEmpty()) {
                    Entity player = players.getFirst();
                    ScoreComponent score = entityManager.getComponent(player, ScoreComponent.class);
                    score.score += 100;
                    score.kills += 1;
                    
                    eventBus.publish(new ScoreEvent(100));
                    
                    float roll = MathUtils.random();
                    if (roll < 0.20f) {
                        // Drop ammo compatible with current player weapon
                        WeaponComponent weapon = entityManager.getComponent(player, WeaponComponent.class);
                        String ammoType = "9mm_regular"; // Use ID, not category
                        if (weapon != null && weapon.activeAmmo != null) {
                            ammoType = weapon.activeAmmo.id;
                        }
                        factory.createAmmoBox("ammo/" + ammoType, 15, x, y);
                    } else if (roll < 0.10f) {
                        factory.createHealthPickup(x, y, 20f);
                    }
                }
            }
            
            health.isDead = true;
            entityManager.removeComponent(victim, AIComponent.class);
            entityManager.removeComponent(victim, ColliderComponent.class);
            entityManager.removeComponent(victim, VelocityComponent.class);
            entityManager.removeComponent(victim, SteeringComponent.class);
        }
    }
}
