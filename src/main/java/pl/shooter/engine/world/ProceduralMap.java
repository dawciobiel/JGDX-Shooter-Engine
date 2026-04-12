package pl.shooter.engine.world;

import java.util.HashMap;
import java.util.Map;

/**
 * Infinite map generated on-the-fly using chunks.
 */
public class ProceduralMap implements GameMap {
    public static final int TILE_SIZE = 32;
    public static final int CHUNK_PIXEL_SIZE = Chunk.SIZE * TILE_SIZE;

    private final Map<String, Chunk> chunks = new HashMap<>();

    @Override
    public float getWidth() {
        return Float.MAX_VALUE;
    }

    @Override
    public float getHeight() {
        return Float.MAX_VALUE;
    }

    @Override
    public boolean isWalkable(float x, float y) {
        Tile tile = getTileAt(x, y);
        return tile != null && tile.walkable;
    }

    @Override
    public float getSpeedMultiplier(float x, float y) {
        Tile tile = getTileAt(x, y);
        return (tile != null) ? tile.speedMultiplier : 1.0f;
    }

    private Tile getTileAt(float x, float y) {
        int cx = (int) Math.floor(x / CHUNK_PIXEL_SIZE);
        int cy = (int) Math.floor(y / CHUNK_PIXEL_SIZE);
        
        int tx = (int) Math.floor((x % CHUNK_PIXEL_SIZE) / TILE_SIZE);
        int ty = (int) Math.floor((y % CHUNK_PIXEL_SIZE) / TILE_SIZE);
        
        // Normalize coordinates if negative
        if (tx < 0) tx += Chunk.SIZE;
        if (ty < 0) ty += Chunk.SIZE;

        Chunk chunk = getChunk(cx, cy);
        return chunk != null ? chunk.getTile(tx, ty) : null;
    }

    public Chunk getChunk(int cx, int cy) {
        String key = cx + "," + cy;
        return chunks.computeIfAbsent(key, k -> new Chunk(cx, cy));
    }
}
