package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Triggered when an entity attempts to shoot without ammo.
 */
public class EmptyWeaponEvent implements Event {
    public final Entity entity;

    public EmptyWeaponEvent(Entity entity) {
        this.entity = entity;
    }
}
