package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as pushable by other entities (usually the player).
 */
public class PushableComponent implements Component {
    public float mass = 1.0f;
    public float friction = 5.0f;
    public boolean snapToAxes = false; // If true, can only be pushed along X or Y axis
    public boolean playerOnly = true;   // Only player can push this object

    public PushableComponent() {}

    public PushableComponent(float mass, float friction) {
        this.mass = mass;
        this.friction = friction;
    }
}
