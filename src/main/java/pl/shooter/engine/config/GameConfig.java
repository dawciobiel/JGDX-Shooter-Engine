package pl.shooter.engine.config;

import com.badlogic.gdx.Input;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Main configuration object for the game.
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameConfig {
    
    public GraphicsConfig graphics = new GraphicsConfig();
    public AudioConfig audio = new AudioConfig();
    public DebugConfig debug = new DebugConfig();
    public InputConfig controls = new InputConfig();
    public EffectsConfig effects = new EffectsConfig();
    public GameplayConfig gameplay = new GameplayConfig();
    public UIConfig ui = new UIConfig();

    public static class GraphicsConfig {
        public int width = 800;
        public int height = 600;
        public boolean fullscreen = false;
        public int targetFps = 60;
        public float ambientBrightness = 0.5f;
        public float ambientRed = 0.1f;
        public float ambientGreen = 0.1f;
        public float ambientBlue = 0.2f;
    }

    public static class AudioConfig {
        public float masterVolume = 1.0f;
        public float sfxVolume = 1.0f;
        public float musicVolume = 0.5f;
    }

    public static class DebugConfig {
        public boolean showHitboxes = false;
        public boolean showFps = true;
        public boolean showPaths = false;
        public boolean invinciblePlayer = false;
    }

    public static class InputConfig {
        public int moveUpKey = Input.Keys.W;
        public int moveDownKey = Input.Keys.S;
        public int moveLeftKey = Input.Keys.A;
        public int moveRightKey = Input.Keys.D;
        public int prevWeaponKey = Input.Keys.Q;
        public int nextWeaponKey = Input.Keys.E;
    }

    public static class EffectsConfig {
        public boolean showBloodDecals = true;
    }

    public static class GameplayConfig {
        public boolean showUnitNames = true;
        public int multiKillThreshold = 3;
        public float multiKillWindow = 3.0f;
    }

    public static class UIConfig {
        public boolean useCustomCursor = true;
        public String cursorImagePath = "ui/crosshairs/image0013.png";
        public float cursorSize = 32f;
        public float cursorRed = 1.0f;
        public float cursorGreen = 1.0f;
        public float cursorBlue = 1.0f;
        public float cursorAlpha = 1.0f;
    }
}
