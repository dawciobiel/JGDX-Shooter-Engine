package pl.shooter.engine.world;

import java.util.Random;

/**
 * A square section of the world containing tiles.
 */
public class Chunk {
    public static final int SIZE = 16;
    private final Tile[][] tiles;
    private static final Random random = new Random();

    public Chunk(int chunkX, int chunkY) {
        this.tiles = new Tile[SIZE][SIZE];
        generate(chunkX, chunkY);
    }

    private void generate(int chunkX, int chunkY) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                // Keep the spawn area clean (Ground)
                if (chunkX == 0 && chunkY == 0 && x >= 10 && x <= 15 && y >= 8 && y <= 12) {
                    tiles[x][y] = Tile.GROUND;
                    continue;
                }

                double roll = random.nextDouble();
                if (roll < 0.10) { 
                    tiles[x][y] = Tile.WALL; 
                } else if (roll < 0.15) {
                    tiles[x][y] = Tile.MUD; // 5% Mud
                } else if (roll < 0.18) {
                    tiles[x][y] = Tile.WATER; // 3% Water
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
