package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.GameplayConfig;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.DeathEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;

/**
 * Tracks rapid kills and broadcasts Multi-Kill messages.
 */
public class MultiKillSystem extends GameSystem {
    private final EventBus eventBus;
    private final GameplayConfig config;
    private int killCount = 0;
    private float killTimer = 0;

    public MultiKillSystem(EntityManager entityManager, EventBus eventBus, GameplayConfig config) {
        super(entityManager);
        this.eventBus = eventBus;
        this.config = config;
        eventBus.subscribe(DeathEvent.class, this::handleDeath);
    }

    @Override
    public void update(float deltaTime) {
        if (killTimer > 0) {
            killTimer -= deltaTime;
            if (killTimer <= 0) {
                killCount = 0;
            }
        }
    }

    private void handleDeath(DeathEvent event) {
        killCount++;
        killTimer = config.multiKillWindow;

        if (killCount >= config.multiKillThreshold) {
            String msg = getMultiKillMessage(killCount);
            eventBus.publish(new MessageEvent(msg, 2.0f));
        }
    }

    private String getMultiKillMessage(int count) {
        return switch (count) {
            case 3 -> "MULTI KILL!";
            case 4 -> "ULTRA KILL!";
            case 5 -> "MONSTER KILL!";
            default -> "UNSTOPPABLE! (" + count + ")";
        };
    }
}
