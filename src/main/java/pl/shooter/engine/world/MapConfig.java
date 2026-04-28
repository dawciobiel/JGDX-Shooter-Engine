package pl.shooter.engine.world;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Modern Map Configuration aggregate.
 * Separates tilemap data, level settings, and entity placements.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapConfig {
    public LevelSettings settings = new LevelSettings();
    public TileLayer tileLayer = new TileLayer();
    public List<EntityEntry> entities;
    public SpawnPoint playerSpawn;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LevelSettings {
        public String mapName = "Unnamed Map";
        public String backgroundTexture;
        public AmbientColor ambientColor = new AmbientColor();
        public StartingEquipment startingEquipment = new StartingEquipment();
        
        public static class AmbientColor {
            public float r = 0.1f, g = 0.1f, b = 0.2f, a = 1.0f;
        }

        public static class StartingEquipment {
            public List<String> weapons;
            public Map<String, Integer> ammo;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TileLayer {
        public String tilesetPath;
        public int tileSize = 32;
        public int displaySize = 32;
        public List<Integer> collidableTiles; // IDs of tiles that have collisions
        public int[][] data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityEntry {
        public String role;       // PLAYER, ENEMY, NEUTRAL, OBJECT
        public String prefabPath; // e.g. "characters/zombie" or "objects/barrel"
        public float x, y;
        public boolean pushable = false;
        public boolean destructible = false;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpawnPoint {
        public float x, y;
    }
}
