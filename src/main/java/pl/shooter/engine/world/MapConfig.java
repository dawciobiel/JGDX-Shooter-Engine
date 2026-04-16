package pl.shooter.engine.world;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Set;

/**
 * Data model for the map.json file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapConfig {
    public String id;
    public String name;
    public Settings settings = new Settings();
    public TileLayer tileLayer;
    public SpawnPoint playerSpawn = new SpawnPoint();
    public List<EntityEntry> entities;

    public static class Settings {
        public float width = 2000;
        public float height = 2000;
        public String backgroundTexture;
        public AmbientColor ambientColor = new AmbientColor();
        public String musicTrack;
    }

    public static class TileLayer {
        public String tilesetPath;
        public int tileSize = 64;
        public int displaySize = 0;
        public int[][] data;
        public Set<Integer> collidableTiles; // IDs of tiles that block movement
    }

    public static class SpawnPoint {
        public float x;
        public float y;
    }

    public static class EntityEntry {
        public String type;
        public float x;
        public float y;
        public float rotation = 0;
    }

    public static class AmbientColor {
        public float r = 0.1f;
        public float g = 0.1f;
        public float b = 0.2f;
        public float a = 0.5f;
    }
}
