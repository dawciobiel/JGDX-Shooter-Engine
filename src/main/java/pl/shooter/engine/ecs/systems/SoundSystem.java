package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SoundComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;
import pl.shooter.events.HitEvent;

/**
 * Listens to game events and plays specific sounds for each entity.
 */
public class SoundSystem extends GameSystem {
    private final AudioService audioService;

    public SoundSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService) {
        super(entityManager);
        this.audioService = audioService;

        // Subscribe to events
        eventBus.subscribe(ShootEvent.class, this::handleShootSound);
        eventBus.subscribe(HitEvent.class, this::handleHitSound);
    }

    private void handleShootSound(ShootEvent event) {
        playSoundForEntity(event.shooter, SoundComponent.Action.SHOOT, 0.4f);
    }

    private void handleHitSound(HitEvent event) {
        playSoundForEntity(event.victim, SoundComponent.Action.HIT, 0.6f);
    }

    private void playSoundForEntity(Entity entity, SoundComponent.Action action, float volume) {
        SoundComponent soundComp = entityManager.getComponent(entity, SoundComponent.class);
        if (soundComp != null) {
            String soundName = soundComp.soundPaths.get(action);
            if (soundName != null) {
                audioService.playSound(soundName, volume);
            }
        }
    }

    @Override
    public void update(float deltaTime) {}
}
