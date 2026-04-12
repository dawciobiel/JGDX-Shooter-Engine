package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Triggered when an entity picks up an item.
 */
public class PickupEvent implements Event {
    public final Entity entity;

    public PickupEvent(Entity entity) {
        this.entity = entity;
    }
}
