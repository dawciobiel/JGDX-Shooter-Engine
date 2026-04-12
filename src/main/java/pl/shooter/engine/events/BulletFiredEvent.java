package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Triggered when a projectile is successfully spawned.
 */
public class BulletFiredEvent implements Event {
    public final Entity shooter;

    public BulletFiredEvent(Entity shooter) {
        this.shooter = shooter;
    }
}
