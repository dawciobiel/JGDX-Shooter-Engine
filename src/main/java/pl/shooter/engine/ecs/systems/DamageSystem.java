package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.HealthComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ScoreEvent;
import pl.shooter.events.HitEvent;

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
        entityManager.removeEntity(event.attacker);

        HealthComponent health = entityManager.getComponent(event.victim, HealthComponent.class);
        TransformComponent trans = entityManager.getComponent(event.victim, TransformComponent.class);

        if (health != null && trans != null) {
            health.hp -= 10;
            factory.createExplosion(trans.x, trans.y, Color.ORANGE);

            if (health.hp <= 0) {
                factory.createExplosion(trans.x, trans.y, Color.RED);
                
                // If the victim was NOT a player, give points
                if (!entityManager.hasComponent(event.victim, PlayerComponent.class)) {
                    eventBus.publish(new ScoreEvent(100));
                }

                entityManager.removeEntity(event.victim);
            }
        }
    }
}
