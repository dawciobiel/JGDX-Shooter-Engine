package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ColliderComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.TriggerComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;
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
                    trigger.isPlayerInside = true;
                    if (trigger.oneShot) trigger.activated = true;
                    
                    // Handle Message type
                    if (trigger.type == TriggerComponent.TriggerType.MESSAGE && trigger.value != null) {
                        eventBus.publish(new MessageEvent(trigger.value, 3.0f));
                    }
                    
                    eventBus.publish(new TriggerEvent(
                        player, 
                        mapType(trigger.type), 
                        trigger.value, 
                        TriggerEvent.State.ENTER,
                        trigger.isLooping,
                        trigger.volume
                    ));
                } else if (!isInside && trigger.isPlayerInside) {
                    trigger.isPlayerInside = false;
                    eventBus.publish(new TriggerEvent(
                        player, 
                        mapType(trigger.type), 
                        trigger.value, 
                        TriggerEvent.State.EXIT,
                        trigger.isLooping,
                        trigger.volume
                    ));
                }
            }
        }
    }

    private TriggerEvent.Type mapType(TriggerComponent.TriggerType type) {
        return switch (type) {
            case AMBIENT_SOUND -> TriggerEvent.Type.AMBIENT_SOUND;
            case STOP_AMBIENT -> TriggerEvent.Type.STOP_AMBIENT;
            case MUSIC_CHANGE -> TriggerEvent.Type.MUSIC_CHANGE;
            case STOP_MUSIC -> TriggerEvent.Type.STOP_MUSIC;
            case TRAP -> TriggerEvent.Type.TRAP;
            case MESSAGE -> TriggerEvent.Type.MESSAGE;
            case ZONE_ENTER -> TriggerEvent.Type.TELEPORT;
        };
    }

    private boolean checkCollision(TransformComponent t1, ColliderComponent c1, TransformComponent t2, ColliderComponent c2) {
        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float distanceSq = dx * dx + dy * dy;
        float radiusSum = c1.radius + c2.radius;
        return distanceSq <= radiusSum * radiusSum;
    }
}
