package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SoundComponent;
import pl.shooter.engine.ecs.components.WeaponComponent;
import pl.shooter.engine.events.BulletFiredEvent;
import pl.shooter.engine.events.EmptyWeaponEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.PickupEvent;
import pl.shooter.events.HitEvent;

/**
 * Listens to game events and plays specific sounds for each entity,
 * supporting dedicated weapon sounds with a default fallback system.
 */
public class SoundSystem extends GameSystem {
    private final AudioService audioService;
    private static final String DEFAULT_WEAPONS_PATH = "assets/audio/sfx/weapons/default/";
    private static final String WEAPONS_BASE_PATH = "assets/audio/sfx/weapons/";

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
        WeaponComponent weapon = entityManager.getComponent(event.shooter, WeaponComponent.class);
        if (weapon != null) {
            String weaponFolder = weapon.type.name().toLowerCase();
            String preferred = WEAPONS_BASE_PATH + weaponFolder + "/" + weaponFolder + ".wav";
            // Also try a generic "shoot.wav" in the weapon folder
            String preferredShoot = WEAPONS_BASE_PATH + weaponFolder + "/shoot.wav";
            String fallback = DEFAULT_WEAPONS_PATH + "shoot.wav";
            
            audioService.playSoundWithFallback(preferred, preferredShoot, 0.4f);
            // If the weapon-named file doesn't exist, try shoot.wav in weapon folder, then default/shoot.wav
            if (!audioService.isSoundLoaded(preferred) && !audioService.isSoundLoaded(preferredShoot)) {
                audioService.playSound(fallback, 0.4f);
            }
        } else {
            playSoundForEntity(event.shooter, SoundComponent.Action.SHOOT, 0.4f);
        }
    }

    private void handleHitSound(HitEvent event) {
        playSoundForEntity(event.victim, SoundComponent.Action.HIT, 0.6f);
    }

    private void handleEmptyClick(EmptyWeaponEvent event) {
        WeaponComponent weapon = entityManager.getComponent(event.entity, WeaponComponent.class);
        if (weapon != null) {
            String weaponFolder = weapon.type.name().toLowerCase();
            String preferred = WEAPONS_BASE_PATH + weaponFolder + "/empty.wav";
            String fallback = DEFAULT_WEAPONS_PATH + "empty.wav";
            audioService.playSoundWithFallback(preferred, fallback, 0.3f);
        } else {
            playSoundForEntity(event.entity, SoundComponent.Action.EMPTY_CLICK, 0.3f);
        }
    }

    private void handlePickupSound(PickupEvent event) {
        // Pickups are usually generic, but we can also use fallback
        String preferred = DEFAULT_WEAPONS_PATH + "pickup.wav";
        audioService.playSound(preferred, 0.5f);
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
