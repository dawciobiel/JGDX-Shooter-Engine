package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages sound effects and background music.
 * Tracks instances via unique ID for debugging.
 */
public class AudioService {
    private final String serviceId;
    private final Map<String, Sound> sounds = new HashMap<>();

    public AudioService() {
        this.serviceId = UUID.randomUUID().toString().substring(0, 8);
        Gdx.app.log("AudioService", "[" + serviceId + "] Created new instance");
    }

    public void loadSound(String path) {
        if (path == null || sounds.containsKey(path)) return;
        
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(path, Gdx.audio.newSound(Gdx.files.internal(path)));
            } else {
                Gdx.app.error("AudioService", "[" + serviceId + "] Sound file missing: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("AudioService", "[" + serviceId + "] CRITICAL ERROR loading sound: " + path, e);
        }
    }

    public long playSound(String path, float volume) {
        if (path == null) return -1;
        Sound s = getOrLoadSound(path);
        return (s != null) ? s.play(volume) : -1;
    }

    public long playLoop(String path, float volume) {
        if (path == null) return -1;
        Sound s = getOrLoadSound(path);
        if (s != null) {
            Gdx.app.log("AudioService", "[" + serviceId + "] Starting loop: " + path);
            return s.loop(volume);
        }
        return -1;
    }

    public void stopAllInstances(String path) {
        Sound s = sounds.get(path);
        if (s != null) {
            Gdx.app.log("AudioService", "[" + serviceId + "] Stopping all instances of: " + path);
            s.stop();
        }
    }

    public void stopInstance(String path, long soundId) {
        Sound s = sounds.get(path);
        if (s != null && soundId != -1) {
            s.stop(soundId);
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

    public void dispose() {
        Gdx.app.log("AudioService", "[" + serviceId + "] Disposing AudioService, clearing " + sounds.size() + " sounds");
        for (Map.Entry<String, Sound> entry : sounds.entrySet()) {
            Gdx.app.log("AudioService", "[" + serviceId + "] Disposing sound object: " + entry.getKey());
            entry.getValue().stop();
            entry.getValue().dispose();
        }
        sounds.clear();
    }
}
