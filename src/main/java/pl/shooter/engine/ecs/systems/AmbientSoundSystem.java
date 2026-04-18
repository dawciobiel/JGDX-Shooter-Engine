package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.TerrainChangeEvent;
import pl.shooter.engine.events.TriggerEvent;
import pl.shooter.engine.world.Tile;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages background ambiance based on terrain and trigger zones.
 * Updated to use resolved paths.
 */
public class AmbientSoundSystem extends GameSystem {
    private final AudioService audioService;
    private final AssetService assetService;
    private final Map<String, Long> activeLoops = new HashMap<>();
    private String currentMusicPath = null;
    private long currentMusicId = -1;

    public AmbientSoundSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService, AssetService assetService) {
        super(entityManager);
        this.audioService = audioService;
        this.assetService = assetService;

        eventBus.subscribe(TerrainChangeEvent.class, this::handleTerrainChange);
        eventBus.subscribe(TriggerEvent.class, this::handleTrigger);
    }

    private void handleTerrainChange(TerrainChangeEvent event) {
        stopAmbiance(getAmbianceForTile(event.oldTile));
        startAmbiance(getAmbianceForTile(event.newTile), 0.5f);
    }

    private void handleTrigger(TriggerEvent event) {
        if (event.type == null) return;
        switch (event.type) {
            case AMBIENT_SOUND:
                if (event.state == TriggerEvent.State.ENTER) {
                    startAmbiance(event.value, 0.7f);
                } else {
                    stopAmbiance(event.value);
                }
                break;
            case MUSIC_CHANGE:
                if (event.state == TriggerEvent.State.ENTER) {
                    playMusic(event.value);
                }
                break;
            case TRAP:
                if (event.state == TriggerEvent.State.ENTER) {
                    audioService.playSound(assetService.resolvePath("traps/trigger.wav", "audio/sfx"), 1.0f);
                }
                break;
        }
    }

    private String getAmbianceForTile(Tile tile) {
        if (tile == null) return null;
        return switch (tile) {
            case WATER -> assetService.resolvePath("water_loop.wav", "audio/ambience");
            case MUD -> assetService.resolvePath("mud_loop.wav", "audio/ambience");
            case METAL -> assetService.resolvePath("metal_loop.wav", "audio/ambience");
            case FIRE -> assetService.resolvePath("fire_loop.wav", "audio/ambience");
            default -> null;
        };
    }

    private void startAmbiance(String path, float volume) {
        if (path == null) return;
        String resolved = assetService.resolvePath(path, "audio/ambience");
        if (activeLoops.containsKey(resolved)) return;

        long id = audioService.playLoop(resolved, volume);
        if (id != -1) activeLoops.put(resolved, id);
    }

    private void stopAmbiance(String path) {
        if (path == null) return;
        String resolved = assetService.resolvePath(path, "audio/ambience");
        Long id = activeLoops.remove(resolved);
        if (id != null) audioService.stopInstance(resolved, id);
    }

    private void playMusic(String path) {
        if (path == null) return;
        String resolved = assetService.resolvePath(path, "audio/music");
        if (resolved.equals(currentMusicPath)) return;
        
        if (currentMusicPath != null && currentMusicId != -1) {
            audioService.stopInstance(currentMusicPath, currentMusicId);
        }

        currentMusicPath = resolved;
        currentMusicId = audioService.playLoop(resolved, 0.6f);
    }

    @Override public void update(float deltaTime) {}
}
