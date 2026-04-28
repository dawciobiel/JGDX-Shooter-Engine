package pl.shooter.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.RenderingConfig;

/**
 * Launcher for the desktop version of the game.
 * Uses ConfigService to determine initial window settings.
 */
public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // Use our new ConfigService to get window settings
        ConfigService configService = new ConfigService();
        RenderingConfig rendering = configService.getRenderingConfig();
        
        config.setForegroundFPS(60); // Default or get from future PerformanceConfig
        config.setTitle("Shooter Game Engine");
        
        if (rendering.fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(rendering.width, rendering.height);
        }
        
        new Lwjgl3Application(new ShooterGame(), config);
    }
}
