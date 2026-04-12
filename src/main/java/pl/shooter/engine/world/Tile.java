package pl.shooter.engine.world;

public enum Tile {
    GROUND(true, 1.0f),
    WALL(false, 0.0f),
    MUD(true, 0.5f),    // Mud slows down movement by 50%
    WATER(true, 0.3f);  // Water slows down movement by 70%

    public final boolean walkable;
    public final float speedMultiplier;

    Tile(boolean walkable, float speedMultiplier) {
        this.walkable = walkable;
        this.speedMultiplier = speedMultiplier;
    }
}
