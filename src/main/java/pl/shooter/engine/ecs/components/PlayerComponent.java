package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marker component to identify the player entity.
 */
public class PlayerComponent implements Component {
    public float speed;

    public PlayerComponent() {} // Required for Jackson JSON deserialization
    public PlayerComponent(float speed) {
        this.speed = speed;
    }
}
