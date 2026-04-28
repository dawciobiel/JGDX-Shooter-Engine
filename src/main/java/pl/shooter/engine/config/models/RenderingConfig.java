package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Visual and display settings. Loaded from config/rendering.toml.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenderingConfig {
    public int width = 800;
    public int height = 600;
    public boolean fullscreen = false;
    public AmbientColor ambientColor = new AmbientColor();
    public UIConfig ui = new UIConfig();

    public static class AmbientColor {
        public float r = 0;
        public float g = 0;
        public float b = 0;
        public float a = 1.0f;
        public float brightness = 1.0f;
    }

    public static class UIConfig {
        public boolean useCustomCursor = false;
        public String cursorImagePath = null;
        public String defaultWeaponIconPath = null;
    }
}
