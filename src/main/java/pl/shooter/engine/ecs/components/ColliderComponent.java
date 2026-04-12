package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

public class ColliderComponent implements Component {
    public float radius;

    public ColliderComponent() {} // Required for JSON
    public ColliderComponent(float radius) {
        this.radius = radius;
    }
}
