package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as an ammo pickup.
 */
public class AmmoPickupComponent implements Component {
    public int amount;
    public String ammoId;

    public AmmoPickupComponent() {}
    public AmmoPickupComponent(String ammoId, int amount) {
        this.ammoId = ammoId;
        this.amount = amount;
    }
}
