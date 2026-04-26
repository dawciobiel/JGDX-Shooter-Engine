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
    public float ambientBrightness = 1.0f;
    public AmbientColor ambientColor = new AmbientColor();
    public UIConfig ui = new UIConfig();

    public static class AmbientColor {
        public float r = 0.1f;
        public float g = 0.1f;
        public float b = 0.2f;
        public float a = 1.0f;
    }

    public static class UIConfig {
        public boolean useCustomCursor = true;
        public String cursorImagePath = "ui/crosshairs/image0013.png";
    }
}
