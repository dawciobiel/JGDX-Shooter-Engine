package pl.shooter.engine.world;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.CharacterPrefab;
import pl.shooter.engine.config.models.PlayerConfig;
import pl.shooter.engine.ecs.EntityFactory;

import java.util.List;

/**
 * Responsible for loading modular map data and pre-loading required assets.
 */
public class MapService {
    private final EntityFactory entityFactory;
    private final AssetService assetService;
    private final ConfigService configService;

    public MapService(EntityFactory entityFactory, AssetService assetService, ConfigService configService) {
        this.entityFactory = entityFactory;
        this.assetService = assetService;
        this.configService = configService;
    }

    public MapConfig loadMap(String mapFolderPath) {
        try {
            MapConfig aggregate = new MapConfig();
            assetService.setCurrentMapFolder(mapFolderPath);

            // 1. Tilemap
            MapConfig.TileLayer tileData = configService.loadAssetConfig(mapFolderPath + "/map.json", MapConfig.TileLayer.class);
            if (tileData != null) aggregate.tileLayer = tileData;

            // 2. Settings
            MapConfig.LevelSettings settings = configService.loadAssetConfig(mapFolderPath + "/config.json", MapConfig.LevelSettings.class);
            if (settings != null) aggregate.settings = settings;

            // 3. Entities & Player Assets
            EntityListWrapper wrapper = configService.loadAssetConfig(mapFolderPath + "/entities.json", EntityListWrapper.class);
            if (wrapper != null && wrapper.entities != null) {
                aggregate.entities = wrapper.entities;
                preloadEntityAssets(aggregate.entities);
            }

            // Preload player assets
            preloadPlayerAssets();

            // Textures
            if (aggregate.tileLayer.tilesetPath != null) {
                assetService.loadTexture(aggregate.tileLayer.tilesetPath);
            }

            return aggregate;
        } catch (Exception e) {
            System.err.println("[MapService] CRITICAL ERROR: " + e.getMessage());
            return null;
        }
    }

    private void preloadPlayerAssets() {
        PlayerConfig pc = configService.loadAssetConfig("assets/global/config/player.json", PlayerConfig.class);
        if (pc != null) {
            CharacterPrefab cp = configService.loadPrefab(pc.characterPrefab, CharacterPrefab.class);
            if (cp != null && cp.visuals != null) {
                assetService.loadTexture(cp.visuals.texturePath);
                // Preload animations if any
                if (cp.visuals.animations != null) {
                    for (CharacterPrefab.AnimationData data : cp.visuals.animations.values()) {
                        if ("FILES".equals(data.type)) {
                            for (int i = 0; i < data.count; i++) {
                                assetService.loadTexture(data.path + "_" + i + ".png");
                            }
                        } else if ("ATLAS_REGION".equals(data.type)) {
                            assetService.loadAtlas(data.path);
                        } else {
                            assetService.loadTexture(data.path);
                        }
                    }
                }
            }
        }
    }

    private void preloadEntityAssets(List<MapConfig.EntityEntry> entities) {
        for (MapConfig.EntityEntry entry : entities) {
            if (entry.prefabPath != null) {
                if ("ENEMY".equalsIgnoreCase(entry.role) || "NEUTRAL".equalsIgnoreCase(entry.role)) {
                    CharacterPrefab prefab = configService.loadPrefab(entry.prefabPath, CharacterPrefab.class);
                    if (prefab != null && prefab.visuals != null) {
                        assetService.loadTexture(prefab.visuals.texturePath);
                        // Also preload animations for enemies
                        if (prefab.visuals.animations != null) {
                            for (CharacterPrefab.AnimationData data : prefab.visuals.animations.values()) {
                            if ("FILES".equals(data.type)) {
                                for (int i = 0; i < data.count; i++) {
                                    assetService.loadTexture(data.path + "_" + i + ".png");
                                }
                            } else if ("ATLAS_REGION".equals(data.type)) {
                                assetService.loadAtlas(data.path);
                            } else {
                                assetService.loadTexture(data.path);
                            }
                            }
                        }
                    }
                }
            }
        }
    }

    public GameMap createGameMap(MapConfig config) {
        return new JsonMap(config);
    }

    public void spawnEntities(MapConfig config) {
        if (config.entities == null) return;
        for (MapConfig.EntityEntry entry : config.entities) {
            switch (entry.role.toUpperCase()) {
                case "PLAYER" -> entityFactory.createPlayer(entry.x, entry.y);
                case "ENEMY" -> entityFactory.createEnemy(entry.prefabPath, entry.x, entry.y);
                case "NEUTRAL" -> entityFactory.createNeutral(entry.prefabPath, entry.x, entry.y);
                case "OBJECT" -> entityFactory.createObject(entry.prefabPath, entry.x, entry.y, entry.pushable, entry.destructible);
            }
        }
    }

    public static class EntityListWrapper {
        public List<MapConfig.EntityEntry> entities;
    }
}
