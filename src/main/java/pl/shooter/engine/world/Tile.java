package pl.shooter.engine.world;

public enum Tile {
    GROUND(true, 1.0f),
    WALL(false, 0.0f),
    MUD(true, 0.5f),
    WATER(true, 0.3f),
    METAL(true, 1.0f),
    FIRE(true, 0.8f); // Fire could slow down or damage (damage logic in future)

    public final boolean walkable;
    public final float speedMultiplier;

    Tile(boolean walkable, float speedMultiplier) {
        this.walkable = walkable;
        this.speedMultiplier = speedMultiplier;
    }
}
