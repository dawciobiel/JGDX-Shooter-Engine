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
        if (entityManager.hasComponent(event.attacker, ProjectileComponent.class)) {
            entityManager.removeEntity(event.attacker);
        }

        HealthComponent health = entityManager.getComponent(event.victim, HealthComponent.class);
        TransformComponent trans = entityManager.getComponent(event.victim, TransformComponent.class);

        if (health != null && trans != null) {
            health.hp -= 10;
            factory.createExplosion(trans.x, trans.y, Color.ORANGE);

            if (health.hp <= 0) {
                onEntityDeath(event.victim, trans.x, trans.y);
            }
        }
    }

    private void onEntityDeath(Entity victim, float x, float y) {
        factory.createExplosion(x, y, Color.RED);
        
        if (entityManager.hasComponent(victim, PlayerComponent.class)) {
            entityManager.removeEntity(victim);
        } else {
            // Enemy death: Score and Drops
            List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
            if (!players.isEmpty()) {
                ScoreComponent score = entityManager.getComponent(players.get(0), ScoreComponent.class);
                score.score += 100;
                score.kills += 1;
            }
            
            eventBus.publish(new ScoreEvent(100));
            
            // Random drops logic
            float roll = MathUtils.random();
            if (roll < 0.20f) {
                // 20% Ammo drop
                factory.createAmmoPickup(x, y, MathUtils.random(5, 15));
            } else if (roll < 0.30f) {
                // 10% Health drop (if roll between 0.20 and 0.30)
                factory.createHealthPickup(x, y, 20f);
            }
            
            entityManager.removeEntity(victim);
        }
    }
}
