package pl.shooter.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;

/**
 * Handles loading and saving of the game configuration.
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
     * Loads the config from user file (local), fallback to default (internal).
     * If user file is missing, it will be created from defaults.
     */
    public void load() {
        FileHandle userFile = Gdx.files.local(USER_PATH);
        FileHandle defaultFile = Gdx.files.internal(DEFAULT_PATH);

        try {
            if (userFile.exists()) {
                config = mapper.readValue(userFile.read(), GameConfig.class);
            } else {
                if (defaultFile.exists()) {
                    config = mapper.readValue(defaultFile.read(), GameConfig.class);
                } else {
                    config = new GameConfig();
                }
                // Auto-create user_config.json on first run
                save();
            }
        } catch (IOException e) {
            Gdx.app.error("ConfigService", "Error loading config: " + e.getMessage());
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
