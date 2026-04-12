package pl.shooter.engine.world;

/**
 * A simple rectangular map with fixed boundaries.
 */
public class StaticMap implements GameMap {
    private final float width;
    private final float height;

    public StaticMap(float width, float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean isWalkable(float x, float y) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }
}
