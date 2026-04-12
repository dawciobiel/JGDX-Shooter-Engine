package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects and background music.
 */
public class AudioService {
    private final Map<String, Sound> sounds = new HashMap<>();

    public void loadSound(String name, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
            } else {
                Gdx.app.error("AudioService", "Sound file not found: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("AudioService", "Error loading sound: " + path);
        }
    }

    public void playSound(String name) {
        Sound s = sounds.get(name);
        if (s != null) {
            s.play();
        }
    }

    public void playSound(String name, float volume) {
        Sound s = sounds.get(name);
        if (s != null) {
            s.play(volume);
        }
    }

    public void dispose() {
        for (Sound s : sounds.values()) {
            s.dispose();
        }
        sounds.clear();
    }
}
