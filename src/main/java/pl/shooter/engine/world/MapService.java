package pl.shooter.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.components.LightComponent;
import pl.shooter.engine.ecs.systems.LightSystem;
import pl.shooter.engine.ecs.systems.RenderSystem;

/**
 * Responsible for loading maps from JSON and initializing the game world.
 */
public class MapService {
    private final EntityManager entityManager;
    private final EntityFactory entityFactory;
    private final ObjectMapper objectMapper;
    private final AssetService assetService;

    public MapService(EntityManager entityManager, EntityFactory entityFactory, AssetService assetService) {
        this.entityManager = entityManager;
        this.entityFactory = entityFactory;
        this.assetService = assetService;
        this.objectMapper = new ObjectMapper();
    }

    public MapConfig loadMap(String mapJsonPath) {
        try {
            FileHandle file = Gdx.files.internal(mapJsonPath);
            if (!file.exists()) {
                Gdx.app.error("MapService", "Map file not found: " + mapJsonPath);
                return null;
            }

            MapConfig config = objectMapper.readValue(file.read(), MapConfig.class);
            
            // Preload textures if any
            if (config.settings.backgroundTexture != null) {
                assetService.loadTexture(config.settings.backgroundTexture);
            }
            if (config.tileLayer != null && config.tileLayer.tilesetPath != null) {
                assetService.loadTexture(config.tileLayer.tilesetPath);
            }
            
            assetService.finishLoading();

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
        // 1. Spawn Player
        if (config.playerSpawn != null) {
            entityFactory.loadFromJson("assets/entities/player.json", config.playerSpawn.x, config.playerSpawn.y);
        }

        // 2. Spawn Static Entities
        if (config.entities != null) {
            for (MapConfig.EntityEntry entry : config.entities) {
                String path = "assets/entities/" + entry.type + ".json";
                entityFactory.loadFromJson(path, entry.x, entry.y);
            }
        }
    }
}
