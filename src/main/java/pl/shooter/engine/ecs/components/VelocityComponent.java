package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

public class VelocityComponent implements Component {
    public float vx;
    public float vy;
    public float terrainMultiplier = 1.0f; // Added to store terrain-based speed modification

    public VelocityComponent() {} // Required for JSON
    public VelocityComponent(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
}
