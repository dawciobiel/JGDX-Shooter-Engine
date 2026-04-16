package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ScoreEvent;
import pl.shooter.engine.events.TauntEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitors kill frequency and triggers TauntEvents for multi-kills.
 */
public class MultiKillSystem extends GameSystem {
    private final EventBus eventBus;
    private final GameConfig config;
    private final List<Float> killTimes = new ArrayList<>();
    private float totalTime = 0;

    public MultiKillSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
        this.config = new ConfigService().getConfig();
        eventBus.subscribe(ScoreEvent.class, this::onKill);
    }

    private void onKill(ScoreEvent event) {
        // We only care about kills (currently ScoreEvent is sent on every kill with points)
        killTimes.add(totalTime);
    }

    @Override
    public void update(float deltaTime) {
        totalTime += deltaTime;
        
        // Remove old kills outside the time window
        killTimes.removeIf(time -> totalTime - time > config.gameplay.multiKillWindow);

        // Check if threshold reached
        if (killTimes.size() >= config.gameplay.multiKillThreshold) {
            eventBus.publish(new TauntEvent());
            killTimes.clear(); // Reset after trigger to avoid spam
        }
    }
}
