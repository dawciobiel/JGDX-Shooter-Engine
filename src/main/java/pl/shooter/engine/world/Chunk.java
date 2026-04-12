package pl.shooter.engine.world;

/**
 * A square section of the world containing tiles.
 */
public class Chunk {
    public static final int SIZE = 16; // 16x16 tiles
    private final Tile[][] tiles;

    public Chunk(int chunkX, int chunkY) {
        this.tiles = new Tile[SIZE][SIZE];
        generate(chunkX, chunkY);
    }

    private void generate(int chunkX, int chunkY) {
        // Very simple generation: mostly ground, random walls
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                // Random wall logic (placeholder)
                if (Math.random() > 0.95) {
                    tiles[x][y] = Tile.WALL;
                } else {
                    tiles[x][y] = Tile.GROUND;
                }
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return Tile.WALL;
        return tiles[x][y];
    }
}
