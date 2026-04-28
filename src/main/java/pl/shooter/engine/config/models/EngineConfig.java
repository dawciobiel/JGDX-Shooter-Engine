package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Core engine settings. Loaded from config/engine.toml.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngineConfig {
    public PathsConfig paths = new PathsConfig();
    public DebugConfig debug = new DebugConfig();
    public PerformanceConfig performance = new PerformanceConfig();

    public static class PathsConfig {
        public String assetsRoot = "assets";
        public String globalRoot = "assets/global";
        public String mapsRoot = "assets/maps";
        
        public String textures = "textures";
        public String audio = "audio";
        public String fonts = "fonts";
        public String shaders = "shaders";
        public String ui = "ui";
        public String materials = "materials";
        public String particles = "particles";
        public String prefabs = "prefabs";
        public String globalConfig = "config";
    }

    public static class DebugConfig {
        public boolean showHitboxes = false;
        public boolean showFps = true;
        public boolean showPaths = false;
        public boolean showProfiler = false;
        public boolean invinciblePlayer = false;
        public boolean infiniteAmmo = false;
    }
    
    public static class PerformanceConfig {
        public int targetFps = 60;
        public int maxGlobalEntities = 100;
    }
}
