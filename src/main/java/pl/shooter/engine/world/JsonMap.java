package pl.shooter.engine.world;

import java.util.HashSet;
import java.util.Set;

/**
 * A concrete implementation of GameMap.
 * Fixed: Now correctly populates collidableTiles from configuration.
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
        if (config.tileLayer != null && config.tileLayer.data != null) {
            this.tileData = config.tileLayer.data;
            this.tileSize = config.tileLayer.tileSize;
            this.displaySize = config.tileLayer.displaySize > 0 ? config.tileLayer.displaySize : 32;
            this.tilesetPath = config.tileLayer.tilesetPath;
            
            int gridHeight = tileData.length;
            int gridWidth = tileData[0].length;
            this.width = gridWidth * displaySize;
            this.height = gridHeight * displaySize;
            
            // FIX: Correctly populate collidable tiles from config
            this.collidableTiles = new HashSet<>();
            if (config.tileLayer.collidableTiles != null) {
                this.collidableTiles.addAll(config.tileLayer.collidableTiles);
            }
        } else {
            this.tileData = null;
            this.tileSize = 32;
            this.displaySize = 32;
            this.tilesetPath = null;
            this.collidableTiles = new HashSet<>();
            this.width = 800;
            this.height = 600;
        }
    }

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }
    public int[][] getTileData() { return tileData; }
    public int getTileSize() { return tileSize; }
    public int getDisplaySize() { return displaySize; }
    public String getTilesetPath() { return tilesetPath; }

    @Override
    public boolean isWalkable(float x, float y) {
        Tile tile = getTile(x, y);
        return tile != null && tile.walkable;
    }

    @Override
    public float getSpeedMultiplier(float x, float y) {
        Tile tile = getTile(x, y);
        return (tile != null) ? tile.speedMultiplier : 1.0f;
    }

    @Override
    public Tile getTile(float x, float y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return Tile.WALL;

        if (tileData != null) {
            int gridX = (int) (x / displaySize);
            int gridY = (int) (y / displaySize);

            if (gridY >= 0 && gridY < tileData.length && gridX >= 0 && gridX < tileData[0].length) {
                int tileId = tileData[gridY][gridX];
                if (collidableTiles.contains(tileId)) {
                    return Tile.WALL;
                } else {
                    return Tile.GROUND;
                }
            }
        }
        return Tile.GROUND;
    }
}
