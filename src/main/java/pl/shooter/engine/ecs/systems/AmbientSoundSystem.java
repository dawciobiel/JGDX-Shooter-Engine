package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
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
 */
public class AmbientSoundSystem extends GameSystem {
    private final AudioService audioService;
    private final Map<String, Long> activeLoops = new HashMap<>();
    private String currentMusicPath = null;
    private long currentMusicId = -1;

    public AmbientSoundSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService) {
        super(entityManager);
        this.audioService = audioService;

        eventBus.subscribe(TerrainChangeEvent.class, this::handleTerrainChange);
        eventBus.subscribe(TriggerEvent.class, this::handleTrigger);
    }

    private void handleTerrainChange(TerrainChangeEvent event) {
        // Stop old terrain sound
        stopAmbiance(getAmbianceForTile(event.oldTile));
        
        // Start new terrain sound
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
                    audioService.playSound("assets/audio/sfx/traps/trigger.wav", 1.0f);
                }
                break;
        }
    }

    private String getAmbianceForTile(Tile tile) {
        if (tile == null) return null;
        return switch (tile) {
            case WATER -> "assets/audio/ambience/water_loop.wav";
            case MUD -> "assets/audio/ambience/mud_loop.wav";
            default -> null;
        };
    }

    private void startAmbiance(String path, float volume) {
        if (path == null || activeLoops.containsKey(path)) return;

        long id = audioService.playLoop(path, volume);
        if (id != -1) {
            activeLoops.put(path, id);
        }
    }

    private void stopAmbiance(String path) {
        if (path == null || !activeLoops.containsKey(path)) return;
        
        Long id = activeLoops.remove(path);
        if (id != null) {
            audioService.stopInstance(path, id);
        }
    }

    private void playMusic(String path) {
        if (path == null || path.equals(currentMusicPath)) return;
        
        // Stop previous music if any
        if (currentMusicPath != null && currentMusicId != -1) {
            audioService.stopInstance(currentMusicPath, currentMusicId);
        }

        currentMusicPath = path;
        currentMusicId = audioService.playLoop(path, 0.6f);
        Gdx.app.log("AmbientSound", "Changing music to: " + path);
    }

    @Override
    public void update(float deltaTime) {
        // Future: Add logic for smooth volume fading
    }
}
