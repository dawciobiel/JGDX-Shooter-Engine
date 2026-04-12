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
 * Handles damage calculation, death, and item drops when a HitEvent is received.
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
            // Enemy death: Add score and increment kills for player
            List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
            if (!players.isEmpty()) {
                ScoreComponent score = entityManager.getComponent(players.get(0), ScoreComponent.class);
                score.score += 100;
                score.kills += 1;
            }
            
            eventBus.publish(new ScoreEvent(100));
            
            if (MathUtils.random() < 0.3f) {
                factory.createAmmoPickup(x, y, MathUtils.random(5, 15));
            }
            
            entityManager.removeEntity(victim);
        }
    }
}
