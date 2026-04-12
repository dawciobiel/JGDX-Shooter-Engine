package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as an ammo pickup.
 */
public class AmmoPickupComponent implements Component {
    public int amount;

    public AmmoPickupComponent() {}
    public AmmoPickupComponent(int amount) {
        this.amount = amount;
    }
}
