package pl.shooter.engine.world;

import java.util.HashSet;
import java.util.Set;

/**
 * A concrete implementation of GameMap that holds dimensions and tile data from JSON.
 * Now supports tile-based collisions.
 */
public class JsonMap implements GameMap {
    private final float width;
    private final float height;
    private final int[][] tileData;
    private final int tileSize;
    private final int displaySize;
    private final String tilesetPath;
    private final Set<Integer> collidableTiles;

    public JsonMap(MapConfig config) {
        this.width = config.settings.width;
        this.height = config.settings.height;
        if (config.tileLayer != null) {
            this.tileData = config.tileLayer.data;
            this.tileSize = config.tileLayer.tileSize;
            this.displaySize = config.tileLayer.displaySize > 0 ? config.tileLayer.displaySize : config.tileLayer.tileSize;
            this.tilesetPath = config.tileLayer.tilesetPath;
            this.collidableTiles = config.tileLayer.collidableTiles != null ? config.tileLayer.collidableTiles : new HashSet<>();
        } else {
            this.tileData = null;
            this.tileSize = 32;
            this.displaySize = 32;
            this.tilesetPath = null;
            this.collidableTiles = new HashSet<>();
        }
    }

    @Override
    public float getWidth() { return width; }

    @Override
    public float getHeight() { return height; }

    public int[][] getTileData() { return tileData; }
    public int getTileSize() { return tileSize; }
    public int getDisplaySize() { return displaySize; }
    public String getTilesetPath() { return tilesetPath; }

    @Override
    public boolean isWalkable(float x, float y) {
        // 1. Bounds check
        if (x < 0 || x >= width || y < 0 || y >= height) return false;

        // 2. Tile collision check
        if (tileData != null) {
            int gridX = (int) (x / displaySize);
            int gridY = (int) (y / displaySize);

            // Check if coordinates are within the defined grid data
            if (gridY >= 0 && gridY < tileData.length && gridX >= 0 && gridX < tileData[0].length) {
                int tileId = tileData[gridY][gridX];
                if (collidableTiles.contains(tileId)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public float getSpeedMultiplier(float x, float y) {
        return 1.0f;
    }
}
