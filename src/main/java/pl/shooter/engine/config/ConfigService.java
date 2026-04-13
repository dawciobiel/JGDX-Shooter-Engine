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
    
    private final ObjectMapper mapper;
    private GameConfig config;

    public ConfigService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        load();
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
}
