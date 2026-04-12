package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Event published when an entity tries to shoot.
 */
public class ShootEvent implements Event {
    public final Entity shooter;
    public final float targetX;
    public final float targetY;

    public ShootEvent(Entity shooter, float targetX, float targetY) {
        this.shooter = shooter;
        this.targetX = targetX;
        this.targetY = targetY;
    }
}
