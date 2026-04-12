package pl.shooter.engine.world;

/**
 * Defines the contract for any game map (static or procedural).
 */
public interface GameMap {
    float getWidth();
    float getHeight();
    
    /**
     * Checks if a specific coordinate is within map bounds and walkable.
     */
    boolean isWalkable(float x, float y);
}
