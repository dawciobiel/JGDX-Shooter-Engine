package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects and background music.
 * Cleaned up to log only on errors.
 */
public class AudioService {
    private final Map<String, Sound> sounds = new HashMap<>();

    public void loadSound(String path) {
        if (path == null || sounds.containsKey(path)) return;
        
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(path, Gdx.audio.newSound(Gdx.files.internal(path)));
            } else {
                Gdx.app.error("AudioService", "Sound file missing: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("AudioService", "CRITICAL ERROR loading sound: " + path, e);
        }
    }

    public boolean isSoundLoaded(String path) {
        return sounds.containsKey(path);
    }

    public long playSound(String path, float volume) {
        if (path == null) return -1;
        Sound s = getOrLoadSound(path);
        return (s != null) ? s.play(volume) : -1;
    }

    public long playLoop(String path, float volume) {
        if (path == null) return -1;
        Sound s = getOrLoadSound(path);
        return (s != null) ? s.loop(volume) : -1;
    }

    public void stopAllInstances(String path) {
        Sound s = sounds.get(path);
        if (s != null) s.stop();
    }

    public void stopInstance(String path, long soundId) {
        Sound s = sounds.get(path);
        if (s != null && soundId != -1) s.stop(soundId);
    }

    public void setVolume(String path, long soundId, float volume) {
        Sound s = sounds.get(path);
        if (s != null && soundId != -1) s.setVolume(soundId, volume);
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
        for (Sound s : sounds.values()) s.dispose();
        sounds.clear();
    }
}
