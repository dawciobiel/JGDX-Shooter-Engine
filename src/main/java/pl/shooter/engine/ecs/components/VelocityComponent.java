package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores velocity data and terrain-based speed modifiers.
 */
public class VelocityComponent implements Component {
    public float vx;
    public float vy;
    public float terrainMultiplier = 1.0f;
    public float baseSpeed = 150f; // Stores the physical speed from character prefab

    public VelocityComponent() {}
    public VelocityComponent(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
}
