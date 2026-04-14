package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects and background music with automatic loading and fallback support.
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
     * Plays a sound. If it's not loaded, it attempts to load it.
     */
    public void playSound(String path, float volume) {
        if (path == null) return;
        
        Sound s = sounds.get(path);
        if (s == null) {
            loadSound(path);
            s = sounds.get(path);
        }
        
        if (s != null) {
            s.play(volume);
        }
    }

    /**
     * Attempts to play a sound from the preferred path. 
     * If it doesn't exist, plays from the fallback path.
     */
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
