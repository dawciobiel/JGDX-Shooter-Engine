package pl.shooter.engine.ecs.components;

import java.util.Map;

/**
 * Data structure for parsing animation definitions from JSON.
 */
public class AnimationConfig {
    public float width;
    public float height;
    public Map<String, StateConfig> states;

    public static class StateConfig {
        public String type; // "SHEET" or "FILES"
        public String path; // Path to sheet or prefix for files
        public int count;   // Number of frames
        public int rows;    // For SHEET type
        public int cols;    // For SHEET type
        public float frameDuration;
    }
}
