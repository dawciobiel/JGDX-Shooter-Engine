package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects and background music with automatic loading.
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

    public void playSound(String path, float volume) {
        Sound s = sounds.get(path);
        if (s != null) {
            s.play(volume);
        } else {
            // Auto-load if not loaded yet
            loadSound(path);
            s = sounds.get(path);
            if (s != null) s.play(volume);
        }
    }

    public void dispose() {
        for (Sound s : sounds.values()) {
            s.dispose();
        }
        sounds.clear();
    }
}
