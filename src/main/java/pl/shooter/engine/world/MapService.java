package pl.shooter.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.JsonService;
import pl.shooter.engine.ecs.EntityFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for loading maps and preloading all required entity assets.
 * Non-blocking: Queues assets in AssetService but doesn't wait for them.
 */
public class MapService {
    private final EntityFactory entityFactory;
    private final ObjectMapper objectMapper;
    private final AssetService assetService;

    public MapService(EntityFactory entityFactory, AssetService assetService) {
        this.entityFactory = entityFactory;
        this.assetService = assetService;
        this.objectMapper = JsonService.getMapper(); // Use Singleton
    }

    public MapConfig loadMap(String mapJsonPath) {
        try {
            FileHandle file = Gdx.files.internal(mapJsonPath);
            if (!file.exists()) return null;

            String mapFolder = file.parent().path();
            assetService.setCurrentMapFolder(mapFolder);
            entityFactory.setCurrentMapFolder(mapFolder);

            MapConfig config = objectMapper.readValue(file.read(), MapConfig.class);
            
            // 1. Queue map-specific textures
            if (config.settings.backgroundTexture != null) assetService.loadTexture(config.settings.backgroundTexture);
            if (config.tileLayer != null && config.tileLayer.tilesetPath != null) assetService.loadTexture(config.tileLayer.tilesetPath);
            
            // 2. Pre-scan entities to queue their assets
            Set<String> typesToLoad = new HashSet<>();
            typesToLoad.add("player");
            if (config.entities != null) {
                for (MapConfig.EntityEntry entry : config.entities) {
                    typesToLoad.add(entry.type);
                }
            }

            for (String type : typesToLoad) {
                entityFactory.loadEntity(type, -1000, -1000); 
            }
            
            return config;
        } catch (Exception e) {
            Gdx.app.error("MapService", "Failed to load map: " + mapJsonPath, e);
            return null;
        }
    }

    public GameMap createGameMap(MapConfig config) {
        return new JsonMap(config);
    }

    public void spawnEntities(MapConfig config) {
        if (config.playerSpawn != null) {
            entityFactory.loadEntity("player", config.playerSpawn.x, config.playerSpawn.y);
        }

        if (config.entities != null) {
            for (MapConfig.EntityEntry entry : config.entities) {
                entityFactory.loadEntity(entry.type, entry.x, entry.y);
            }
        }
    }
}
