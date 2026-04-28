package pl.shooter.engine.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.config.models.*;

import java.io.File;
import java.io.FileInputStream;

/**
 * Advanced Config Service with Verbose Logging for Debugging.
 */
public class ConfigService {
    private final ObjectMapper jsonMapper;
    private final ObjectMapper tomlMapper;
    
    private EngineConfig engineConfig;
    private RenderingConfig renderingConfig;
    private PhysicsConfig physicsConfig;
    private InputConfig userInputConfig;
    private GameplayConfig gameplayConfig;
    private InputConfig defaultInputConfig;

    public ConfigService() {
        this.jsonMapper = JsonService.getMapper();
        this.tomlMapper = JsonService.getTomlMapper();
    }

    public EngineConfig getEngineConfig() {
        if (engineConfig == null) {
            engineConfig = loadExternalConfig("engine.toml", EngineConfig.class);
            if (engineConfig == null) engineConfig = new EngineConfig();
        }
        return engineConfig;
    }

    public RenderingConfig getRenderingConfig() {
        if (renderingConfig == null) {
            renderingConfig = loadExternalConfig("rendering.toml", RenderingConfig.class);
            if (renderingConfig == null) renderingConfig = new RenderingConfig();
        }
        return renderingConfig;
    }

    public PhysicsConfig getPhysicsConfig() {
        if (physicsConfig == null) {
            physicsConfig = loadExternalConfig("physics.toml", PhysicsConfig.class);
            if (physicsConfig == null) physicsConfig = new PhysicsConfig();
        }
        return physicsConfig;
    }

    public InputConfig getUserInputConfig() {
        if (userInputConfig == null) {
            userInputConfig = loadExternalConfig("input.toml", InputConfig.class);
        }
        return userInputConfig;
    }

    public GameplayConfig getGameplayConfig() {
        if (gameplayConfig == null) {
            String path = "assets/global/config/game.json";
            gameplayConfig = loadAssetConfig(path, GameplayConfig.class);
            if (gameplayConfig == null) gameplayConfig = new GameplayConfig();
        }
        return gameplayConfig;
    }

    public InputConfig getDefaultInputConfig() {
        if (defaultInputConfig == null) {
            String path = "assets/global/config/input.json";
            defaultInputConfig = loadAssetConfig(path, InputConfig.class);
            if (defaultInputConfig == null) defaultInputConfig = new InputConfig();
        }
        return defaultInputConfig;
    }

    private <T> T loadExternalConfig(String fileName, Class<T> dataClass) {
        String fullPath = "config/" + fileName;
        File file = new File(fullPath);
        if (file.exists()) {
            try (FileInputStream is = new FileInputStream(file)) {
                JavaType type = tomlMapper.getTypeFactory().constructParametricType(DataWrapper.class, dataClass);
                DataWrapper<T> wrapper = tomlMapper.readValue(is, type);
                return wrapper.data;
            } catch (Exception e) {
                System.err.println("[ConfigService] ERROR loading external TOML " + fullPath + ": " + e.getMessage());
            }
        }
        return null;
    }

    public <T> T loadAssetConfig(String internalPath, Class<T> dataClass) {
        if (Gdx.files == null) return null;
        FileHandle handle = Gdx.files.internal(internalPath);
        if (handle.exists()) {
            return unwrap(handle, dataClass, jsonMapper);
        } else {
            System.err.println("[ConfigService] Asset file NOT FOUND: " + internalPath);
        }
        return null;
    }

    private <T> T unwrap(FileHandle file, Class<T> dataClass, ObjectMapper currentMapper) {
        try {
            JavaType type = currentMapper.getTypeFactory().constructParametricType(DataWrapper.class, dataClass);
            DataWrapper<T> wrapper = currentMapper.readValue(file.read(), type);
            if (wrapper == null || wrapper.data == null) {
                System.err.println("[ConfigService] FAILED UNWRAP (data is null): " + file.path());
                return null;
            }
            return wrapper.data;
        } catch (Exception e) {
            System.err.println("[ConfigService] ERROR unwrapping " + file.path() + ": " + e.getMessage());
            return null;
        }
    }

    public <T> T loadPrefab(String prefabPath, Class<T> prefabClass) {
        String fullPath = "assets/global/prefabs/" + prefabPath + ".json"; //fixme Ścieżka `assets/global/prefabs/` jest wpisana na sztywno w kodzie.
        return loadAssetConfig(fullPath, prefabClass);
    }
}
