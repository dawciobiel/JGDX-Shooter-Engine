package pl.shooter.engine.world;

/**
 * A square section of the world containing tiles.
 */
public class Chunk {
    public static final int SIZE = 16;
    private final Tile[][] tiles;

    public Chunk(int chunkX, int chunkY) {
        this.tiles = new Tile[SIZE][SIZE];
        generate(chunkX, chunkY);
    }

    private void generate(int chunkX, int chunkY) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                // Ensure starting area (around 400,300) is clear
                // 400px / 32px per tile = ~12.5 tile in chunk 0,0
                if (chunkX == 0 && chunkY == 0 && x > 10 && x < 15 && y > 8 && y < 12) {
                    tiles[x][y] = Tile.GROUND;
                    continue;
                }

                if (Math.random() > 0.90) { // 10% chance for a wall
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
