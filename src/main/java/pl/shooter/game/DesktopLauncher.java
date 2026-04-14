package pl.shooter.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.config.GameConfig;

import java.io.File;
import java.io.IOException;

/**
 * Launcher for the desktop version of the game.
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        GameConfig gameConfig = loadConfig();
        
        config.setForegroundFPS(gameConfig.graphics.targetFps);
        config.setTitle("Shooter Game Engine");
        
        if (gameConfig.graphics.fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(gameConfig.graphics.width, gameConfig.graphics.height);
        }
        
        new Lwjgl3Application(new ShooterGame(), config);
    }

    private static GameConfig loadConfig() {
        ObjectMapper mapper = new ObjectMapper();
        GameConfig config = new GameConfig();
        
        try {
            // 1. Load defaults from file if it exists
            File defaultFile = new File("assets/config/default_config.json");
            if (defaultFile.exists()) {
                config = mapper.readerForUpdating(config).readValue(defaultFile);
            }
            
            // 2. Override with user settings if they exist
            File userFile = new File("user_config.json");
            if (userFile.exists()) {
                config = mapper.readerForUpdating(config).readValue(userFile);
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration in DesktopLauncher: " + e.getMessage());
        }
        
        return config;
    }
}
