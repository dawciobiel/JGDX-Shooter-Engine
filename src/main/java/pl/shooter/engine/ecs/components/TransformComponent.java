package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores the position and rotation of an entity in the game world.
 */
public class TransformComponent implements Component {
    public float x;
    public float y;
    public float rotation; // in degrees

    public TransformComponent() {} // Required for JSON

    public TransformComponent(float x, float y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
    }

    public TransformComponent(float x, float y, float rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
}
