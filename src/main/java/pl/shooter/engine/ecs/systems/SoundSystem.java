package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SoundComponent;
import pl.shooter.engine.events.BulletFiredEvent;
import pl.shooter.engine.events.EmptyWeaponEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.PickupEvent;
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
        eventBus.subscribe(BulletFiredEvent.class, this::handleBulletFired);
        eventBus.subscribe(HitEvent.class, this::handleHitSound);
        eventBus.subscribe(EmptyWeaponEvent.class, this::handleEmptyClick);
        eventBus.subscribe(PickupEvent.class, this::handlePickupSound);
    }

    private void handleBulletFired(BulletFiredEvent event) {
        playSoundForEntity(event.shooter, SoundComponent.Action.SHOOT, 0.4f);
    }

    private void handleHitSound(HitEvent event) {
        playSoundForEntity(event.victim, SoundComponent.Action.HIT, 0.6f);
    }

    private void handleEmptyClick(EmptyWeaponEvent event) {
        playSoundForEntity(event.entity, SoundComponent.Action.EMPTY_CLICK, 0.3f);
    }

    private void handlePickupSound(PickupEvent event) {
        playSoundForEntity(event.entity, SoundComponent.Action.PICKUP, 0.5f);
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
