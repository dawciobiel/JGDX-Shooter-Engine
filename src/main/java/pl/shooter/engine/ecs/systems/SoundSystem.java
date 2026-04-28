package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.SoundComponent;
import pl.shooter.engine.events.*;
import pl.shooter.events.HitEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens to game events and plays specific sounds.
 */
public class SoundSystem extends GameSystem {
    private final AudioService audioService;
    private final AssetService assetService;
    private final List<String> tauntFiles = new ArrayList<>();
    private boolean tauntsDiscovered = false;

    public SoundSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService, AssetService assetService) {
        super(entityManager);
        this.audioService = audioService;
        this.assetService = assetService;

        eventBus.subscribe(BulletFiredEvent.class, this::handleBulletFired);
        eventBus.subscribe(HitEvent.class, this::handleHitSound);
        eventBus.subscribe(DeathEvent.class, this::handleDeathSound);
        eventBus.subscribe(EmptyWeaponEvent.class, this::handleEmptyClick);
        eventBus.subscribe(PickupEvent.class, this::handlePickupSound);
        eventBus.subscribe(TauntEvent.class, event -> handleTaunt());
    }

    @Override
    public void update(float deltaTime) {
        if (!tauntsDiscovered) {
            discoverTaunts();
            tauntsDiscovered = true;
        }
    }

    private void discoverTaunts() {
        // Taunts are optional, resolvePath will log error if missing, but we handle it
        String resolvedDir = assetService.resolvePath("characters/taunt", "audio/sfx");
        if (resolvedDir == null) {
            System.out.println("[SoundSystem] No taunts discovered for this map/setup.");
            return;
        }

        try {
            FileHandle dir = Gdx.files.internal(resolvedDir);
            if (dir.exists() && dir.isDirectory()) {
                tauntFiles.clear();
                for (FileHandle file : dir.list()) {
                    if (file.extension().equalsIgnoreCase("wav") || file.extension().equalsIgnoreCase("mp3")) {
                        tauntFiles.add(file.path());
                        audioService.loadSound(file.path());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SoundSystem] Error listing taunts: " + e.getMessage());
        }
    }

    private void handleTaunt() {
        if (!tauntFiles.isEmpty()) {
            String randomTaunt = tauntFiles.get(MathUtils.random(tauntFiles.size() - 1));
            audioService.playSound(randomTaunt, 0.8f);
        }
    }

    private void handleBulletFired(BulletFiredEvent event) {
        String fallbackSound = assetService.resolvePath("weapons/default/shoot.wav", "audio/sfx");
        if (event.soundPath != null && !event.soundPath.equals("null")) {
            String resolved = assetService.resolvePath(event.soundPath, "audio/sfx");
            if (resolved != null && Gdx.files.internal(resolved).exists()) {
                audioService.playSound(resolved, 0.4f);
                return;
            }
        }
        if (fallbackSound != null) audioService.playSound(fallbackSound, 0.3f);
    }

    private void handleHitSound(HitEvent event) {
        SoundComponent sc = entityManager.getComponent(event.victim, SoundComponent.class);
        if (sc != null && sc.soundPaths.containsKey(SoundComponent.Action.HIT)) {
            String soundName = sc.soundPaths.get(SoundComponent.Action.HIT);
            String resolved = assetService.resolvePath(soundName, "audio/sfx");
            if (resolved != null) audioService.playSound(resolved, 0.6f);
        }
    }

    private void handleDeathSound(DeathEvent event) {
        SoundComponent sc = entityManager.getComponent(event.victim, SoundComponent.class);
        if (sc != null && sc.soundPaths.containsKey(SoundComponent.Action.DIE)) {
            String soundName = sc.soundPaths.get(SoundComponent.Action.DIE);
            String resolved = assetService.resolvePath(soundName, "audio/sfx");
            if (resolved != null) audioService.playSound(resolved, 0.8f);
        }
    }

    private void handleEmptyClick(EmptyWeaponEvent event) {
        String sound = assetService.resolvePath("weapons/default/empty.wav", "audio/sfx");
        if (sound != null) audioService.playSound(sound, 0.3f);
    }

    private void handlePickupSound(PickupEvent event) {
        String sound = assetService.resolvePath("weapons/default/pickup.wav", "audio/sfx");
        if (sound != null) audioService.playSound(sound, 0.5f);
    }

    @Override public void dispose() {
        tauntFiles.clear();
    }
}
