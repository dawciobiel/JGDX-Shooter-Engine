package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.events.HitEvent;

/**
 * Listens to game events and plays corresponding sounds.
 */
public class SoundSystem extends GameSystem {
    private final AudioService audioService;

    public SoundSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService) {
        super(entityManager);
        this.audioService = audioService;

        // Subscribe to events
        eventBus.subscribe(ShootEvent.class, event -> audioService.playSound("shoot", 0.5f));
        eventBus.subscribe(HitEvent.class, event -> audioService.playSound("hit", 0.7f));
    }

    @Override
    public void update(float deltaTime) {
        // Sound system doesn't need per-frame logic for simple SFX
    }
}
