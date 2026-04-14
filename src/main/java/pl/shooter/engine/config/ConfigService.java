package pl.shooter.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;

/**
 * Handles loading and saving of the game configuration.
 * Now safely merges user configuration with default settings.
 */
public class ConfigService {
    private static final String DEFAULT_PATH = "assets/config/default_config.json";
    private static final String USER_PATH = "user_config.json"; 
    private static final String WEAPONS_PATH = "assets/config/weapons.json";
    
    private final ObjectMapper mapper;
    private GameConfig config;
    private WeaponConfig weaponConfig;

    public ConfigService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        load();
        loadWeapons();
    }

    /**
     * Loads the config by first loading defaults, then merging with user file.
     * If user file is missing, it will be created from defaults.
     */
    public void load() {
        FileHandle userFile = Gdx.files.local(USER_PATH);
        FileHandle defaultFile = Gdx.files.internal(DEFAULT_PATH);

        try {
            // 1. Load default config
            if (defaultFile.exists()) {
                config = mapper.readValue(defaultFile.read(), GameConfig.class);
            } else {
                config = new GameConfig(); // Fallback to hardcoded defaults if default file is missing
                Gdx.app.error("ConfigService", "Default config file missing! Using hardcoded defaults.");
            }

            // 2. Merge with user config if it exists
            if (userFile.exists()) {
                // Use readerForUpdating to merge existing config with user's changes
                config = mapper.readerForUpdating(config).readValue(userFile.read());
            } else {
                // If user file doesn't exist, create it based on the (now merged) default config
                save();
            }
        } catch (IOException e) {
            Gdx.app.error("ConfigService", "Error loading config: " + e.getMessage());
            // If any error during load, ensure we have a working config
            config = new GameConfig();
        }
    }

    private void loadWeapons() {
        FileHandle weaponFile = Gdx.files.internal(WEAPONS_PATH);
        try {
            if (weaponFile.exists()) {
                weaponConfig = mapper.readValue(weaponFile.read(), WeaponConfig.class);
                Gdx.app.log("ConfigService", "Weapon config loaded: " + weaponConfig.weapons.size() + " types.");
            } else {
                weaponConfig = new WeaponConfig();
                Gdx.app.error("ConfigService", "Weapon config file missing!");
            }
        } catch (IOException e) {
            Gdx.app.error("ConfigService", "Error loading weapon config: " + e.getMessage());
            weaponConfig = new WeaponConfig();
        }
    }

    /**
     * Saves the current config to the user file.
     */
    public void save() {
        FileHandle userFile = Gdx.files.local(USER_PATH);
        try {
            String json = mapper.writeValueAsString(config);
            userFile.writeString(json, false);
            Gdx.app.log("ConfigService", "Config saved to: " + userFile.path());
        } catch (IOException e) {
            Gdx.app.error("ConfigService", "Error saving config: " + e.getMessage());
        }
    }

    public GameConfig getConfig() {
        return config;
    }

    public WeaponConfig getWeaponConfig() {
        return weaponConfig;
    }
}
