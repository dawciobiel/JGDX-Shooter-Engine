package pl.shooter.engine.world;

public enum Tile {
    GROUND(true),
    WALL(false);

    public final boolean walkable;

    Tile(boolean walkable) {
        this.walkable = walkable;
    }
}
