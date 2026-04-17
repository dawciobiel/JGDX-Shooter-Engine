package pl.shooter.engine.world;

/**
 * A static map designed for testing AI and Pathfinding.
 * It contains specific obstacles to force the AI to find routes.
 */
public class TestingMap implements GameMap {
    private final int widthInTiles = 50;
    private final int heightInTiles = 50;
    private final boolean[][] walkable;

    public TestingMap() {
        walkable = new boolean[widthInTiles][heightInTiles];
        
        // 1. Initialize all as walkable
        for (int x = 0; x < widthInTiles; x++) {
            for (int y = 0; y < heightInTiles; y++) {
                walkable[x][y] = true;
            }
        }

        // 2. Create a "U-shaped" wall in the middle to test pathfinding around it
        // Vertical left side of U
        int midX = widthInTiles / 2;
        int midY = heightInTiles / 2;
        
        for (int y = midY - 5; y <= midY + 5; y++) {
            walkable[midX - 5][y] = false;
        }
        // Vertical right side of U
        for (int y = midY - 5; y <= midY + 5; y++) {
            walkable[midX + 5][y] = false;
        }
        // Horizontal bottom of U
        for (int x = midX - 5; x <= midX + 5; x++) {
            walkable[x][midY + 5] = false;
        }
        
        // 3. Add some pillars
        walkable[10][10] = false;
        walkable[10][11] = false;
        walkable[11][10] = false;
        walkable[11][11] = false;
        
        walkable[40][40] = false;
        walkable[40][41] = false;
        walkable[41][40] = false;
        walkable[41][41] = false;
    }

    @Override
    public float getWidth() {
        return widthInTiles * 32f;
    }

    @Override
    public float getHeight() {
        return heightInTiles * 32f;
    }

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
        int gx = (int) Math.floor(x / 32f);
        int gy = (int) Math.floor(y / 32f);
        
        if (gx < 0 || gx >= widthInTiles || gy < 0 || gy >= heightInTiles) {
            return Tile.WALL;
        }
        return walkable[gx][gy] ? Tile.GROUND : Tile.WALL;
    }
}
