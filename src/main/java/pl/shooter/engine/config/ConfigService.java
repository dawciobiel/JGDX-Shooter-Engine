package pl.shooter.engine.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

/**
 * Service for loading and saving game configuration.
 * Uses shared ObjectMapper from JsonService.
 */
public class ConfigService {
    private static final String DEFAULT_CONFIG_PATH = "assets/config/default_config.json";
    private static final String USER_CONFIG_PATH = "user_config.json";
    private final ObjectMapper mapper;
    private GameConfig cachedConfig;

    public ConfigService() {
        this.mapper = JsonService.getMapper(); // Use Singleton
    }

    public GameConfig getConfig() {
        if (cachedConfig != null) return cachedConfig;

        GameConfig config = loadConfig(DEFAULT_CONFIG_PATH);
        if (config == null) config = new GameConfig();

        GameConfig userConfig = loadConfig(USER_CONFIG_PATH);
        if (userConfig != null) {
            mergeConfigs(config, userConfig);
        }

        cachedConfig = config;
        return cachedConfig;
    }

    public WeaponConfig getWeaponConfig() {
        try {
            FileHandle file = Gdx.files.internal("assets/config/weapons.json");
            if (file.exists()) {
                return mapper.readValue(file.read(), WeaponConfig.class);
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigService", "Error loading weapons.json", e);
        }
        return new WeaponConfig();
    }

    private GameConfig loadConfig(String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                return mapper.readValue(file.read(), GameConfig.class);
            } else if (new File(path).exists()) {
                return mapper.readValue(new File(path), GameConfig.class);
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigService", "Error loading config: " + e.getMessage());
        }
        return null;
    }

    private void mergeConfigs(GameConfig base, GameConfig user) {
        if (user.graphics != null) {
            if (user.graphics.width > 0) base.graphics.width = user.graphics.width;
            if (user.graphics.height > 0) base.graphics.height = user.graphics.height;
            base.graphics.fullscreen = user.graphics.fullscreen;
        }
        if (user.debug != null) {
            base.debug.showHitboxes = user.debug.showHitboxes;
            base.debug.showFps = user.debug.showFps;
            base.debug.showPaths = user.debug.showPaths;
        }
        if (user.ui != null) {
            base.ui.useCustomCursor = user.ui.useCustomCursor;
            if (user.ui.cursorImagePath != null) base.ui.cursorImagePath = user.ui.cursorImagePath;
            base.ui.cursorSize = user.ui.cursorSize;
            base.ui.cursorAlpha = user.ui.cursorAlpha;
        }
    }

    public void saveUserConfig(GameConfig config) {
        try {
            mapper.writeValue(new File(USER_CONFIG_PATH), config);
        } catch (Exception e) {
            Gdx.app.error("ConfigService", "Error saving user config", e);
        }
    }
}
