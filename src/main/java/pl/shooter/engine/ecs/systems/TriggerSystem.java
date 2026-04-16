package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ColliderComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.TriggerComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.TriggerEvent;

import java.util.List;

/**
 * Monitors collisions between players and trigger zones.
 */
public class TriggerSystem extends GameSystem {
    private final EventBus eventBus;

    public TriggerSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class, ColliderComponent.class);
        List<Entity> triggers = entityManager.getEntitiesWithComponents(TriggerComponent.class, TransformComponent.class, ColliderComponent.class);

        if (players.isEmpty() || triggers.isEmpty()) return;

        for (Entity player : players) {
            TransformComponent pt = entityManager.getComponent(player, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(player, ColliderComponent.class);

            for (Entity triggerEntity : triggers) {
                TriggerComponent trigger = entityManager.getComponent(triggerEntity, TriggerComponent.class);
                
                if (trigger.oneShot && trigger.activated) continue;

                TransformComponent tt = entityManager.getComponent(triggerEntity, TransformComponent.class);
                ColliderComponent tc = entityManager.getComponent(triggerEntity, ColliderComponent.class);

                boolean isInside = checkCollision(pt, pc, tt, tc);

                if (isInside && !trigger.isPlayerInside) {
                    // Entered
                    trigger.isPlayerInside = true;
                    if (trigger.oneShot) trigger.activated = true;
                    eventBus.publish(new TriggerEvent(player, triggerEntity, TriggerEvent.State.ENTER, trigger));
                } else if (!isInside && trigger.isPlayerInside) {
                    // Exited
                    trigger.isPlayerInside = false;
                    eventBus.publish(new TriggerEvent(player, triggerEntity, TriggerEvent.State.EXIT, trigger));
                }
            }
        }
    }

    private boolean checkCollision(TransformComponent t1, ColliderComponent c1, TransformComponent t2, ColliderComponent c2) {
        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float distanceSq = dx * dx + dy * dy;
        float radiusSum = c1.radius + c2.radius;
        return distanceSq <= radiusSum * radiusSum;
    }
}
