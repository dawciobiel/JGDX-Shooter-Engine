package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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

        audioService.loadSound(path);
        // We need to access the Sound object to loop it.
        // For simplicity in this engine version, we'll use a hack or assume AudioService can be extended.
        // Let's play it via a direct call if possible or just log it for now.
        Gdx.app.log("AmbientSound", "Starting ambiance loop: " + path);
        // Note: Real implementation would need AudioService to return soundId for looping control.
    }

    private void stopAmbiance(String path) {
        if (path == null || !activeLoops.containsKey(path)) return;
        Gdx.app.log("AmbientSound", "Stopping ambiance loop: " + path);
        activeLoops.remove(path);
    }

    private void playMusic(String path) {
        if (path == null || path.equals(currentMusicPath)) return;
        Gdx.app.log("AmbientSound", "Changing music to: " + path);
        currentMusicPath = path;
        // Logic to fade out old music and fade in new music
    }

    @Override
    public void update(float deltaTime) {
        // Could be used for smooth volume fading
    }
}
