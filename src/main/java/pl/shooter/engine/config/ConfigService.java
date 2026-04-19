package pl.shooter.engine.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

/**
 * Service for loading and saving game configuration.
 * Handles Engine, Input, and Weapon configurations.
 */
public class ConfigService {
    private static final String DEFAULT_CONFIG_PATH = "assets/configs/engine_config.json";
    private static final String USER_CONFIG_PATH = "user_config.json";
    private final ObjectMapper mapper;
    private GameConfig cachedConfig;
    private InputConfig cachedInputConfig;
    private WeaponConfig cachedWeaponConfig;

    public ConfigService() {
        this.mapper = JsonService.getMapper();
    }

    public GameConfig getConfig() {
        if (cachedConfig != null) return cachedConfig;

        GameConfig config = loadConfig(DEFAULT_CONFIG_PATH, GameConfig.class);
        if (config == null) config = new GameConfig();

        GameConfig userConfig = loadConfig(USER_CONFIG_PATH, GameConfig.class);
        if (userConfig != null) {
            mergeConfigs(config, userConfig);
        }

        cachedConfig = config;
        return cachedConfig;
    }

    public InputConfig getInputConfig() {
        if (cachedInputConfig != null) return cachedInputConfig;

        String path = getConfig().paths.inputConfigPath;
        InputConfig config = loadConfig(path, InputConfig.class);
        if (config == null) {
            config = new InputConfig();
        }
        
        cachedInputConfig = config;
        return cachedInputConfig;
    }

    public WeaponConfig getWeaponConfig() {
        if (cachedWeaponConfig == null) {
            cachedWeaponConfig = new WeaponConfig();
        }
        return cachedWeaponConfig;
    }

    public void loadWeaponConfigForMap(String mapFolder) {
        WeaponConfig fullConfig = new WeaponConfig();
        String weaponsFolder = mapFolder + "/configs/weapons";
        FileHandle dir = Gdx.files.internal(weaponsFolder);

        if (dir.exists() && dir.isDirectory()) {
            for (FileHandle file : dir.list(".json")) {
                try {
                    WeaponConfig.WeaponData data = mapper.readValue(file.read(), WeaponConfig.WeaponData.class);
                    String weaponName = file.nameWithoutExtension().toUpperCase();
                    fullConfig.weapons.put(weaponName, data);
                } catch (Exception e) {
                    Gdx.app.error("ConfigService", "Error loading weapon file: " + file.name(), e);
                }
            }
        } else {
            Gdx.app.error("ConfigService", "Weapons folder NOT FOUND: " + weaponsFolder);
        }
        
        this.cachedWeaponConfig = fullConfig;
    }

    private <T> T loadConfig(String path, Class<T> clazz) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                return mapper.readValue(file.read(), clazz);
            } else if (new File(path).exists()) {
                return mapper.readValue(new File(path), clazz);
            }
        } catch (Exception e) {
            Gdx.app.error("ConfigService", "Error loading config " + path + ": " + e.getMessage());
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
