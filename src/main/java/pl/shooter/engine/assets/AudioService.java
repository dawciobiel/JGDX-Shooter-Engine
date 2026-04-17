package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects and background music with automatic loading and support for looping.
 */
public class AudioService {
    private final Map<String, Sound> sounds = new HashMap<>();

    public void loadSound(String path) {
        if (sounds.containsKey(path)) return;
        
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(path, Gdx.audio.newSound(Gdx.files.internal(path)));
                Gdx.app.log("AudioService", "Loaded sound: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("AudioService", "Error loading sound: " + path);
        }
    }

    public boolean isSoundLoaded(String path) {
        return sounds.containsKey(path);
    }

    /**
     * Plays a sound once.
     */
    public long playSound(String path, float volume) {
        if (path == null) return -1;
        
        Sound s = getOrLoadSound(path);
        if (s != null) {
            return s.play(volume);
        }
        return -1;
    }

    /**
     * Plays a sound in a loop. Returns the sound instance ID.
     */
    public long playLoop(String path, float volume) {
        if (path == null) return -1;

        Sound s = getOrLoadSound(path);
        if (s != null) {
            long id = s.loop(volume);
            Gdx.app.log("AudioService", "Started loop: " + path + " (ID: " + id + ")");
            return id;
        }
        return -1;
    }

    /**
     * Stops all instances of a specific sound.
     */
    public void stopAllInstances(String path) {
        Sound s = sounds.get(path);
        if (s != null) {
            s.stop();
        }
    }

    /**
     * Stops a specific instance of a sound.
     */
    public void stopInstance(String path, long soundId) {
        Sound s = sounds.get(path);
        if (s != null && soundId != -1) {
            s.stop(soundId);
        }
    }

    /**
     * Changes the volume of a specific sound instance.
     */
    public void setVolume(String path, long soundId, float volume) {
        Sound s = sounds.get(path);
        if (s != null && soundId != -1) {
            s.setVolume(soundId, volume);
        }
    }

    private Sound getOrLoadSound(String path) {
        Sound s = sounds.get(path);
        if (s == null) {
            loadSound(path);
            s = sounds.get(path);
        }
        return s;
    }

    public void playSoundWithFallback(String preferredPath, String fallbackPath, float volume) {
        if (preferredPath != null && Gdx.files.internal(preferredPath).exists()) {
            playSound(preferredPath, volume);
        } else if (fallbackPath != null) {
            playSound(fallbackPath, volume);
        }
    }

    public void dispose() {
        for (Sound s : sounds.values()) {
            s.dispose();
        }
        sounds.clear();
    }
}
