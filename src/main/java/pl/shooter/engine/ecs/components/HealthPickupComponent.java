package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as a health pickup.
 */
public class HealthPickupComponent implements Component {
    public float amount;

    public HealthPickupComponent() {}
    public HealthPickupComponent(float amount) {
        this.amount = amount;
    }
}
