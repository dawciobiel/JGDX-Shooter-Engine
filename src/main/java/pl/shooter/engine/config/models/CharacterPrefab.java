package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Defines a visual and physical template for a character (Player, Enemy, NPC).
 * This is the "implementation" layer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterPrefab {
    public String name;
    public Stats stats = new Stats();
    public Visuals visuals = new Visuals();
    public Audio visualsAudio = new Audio();

    public static class Stats {
        public float health = 100f;
        public float speed = 150f;
        public float radius = 16f; // Collision radius
    }

    public static class Visuals {
        public String texturePath;
        public int frameWidth;
        public int frameHeight;
        public Map<String, AnimationData> animations;
    }

    public static class AnimationData {
        public String path;
        public String type; // "SHEET" or "FILES"
        public String region;
        public int rows = 1;
        public int cols = 1;
        public int count = 0;
        public float frameDuration = 0.1f;
    }

    public static class Audio {
        public Map<String, String> sounds; // ActionName -> FilePath
    }
}
