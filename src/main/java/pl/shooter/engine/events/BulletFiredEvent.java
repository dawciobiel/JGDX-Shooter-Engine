package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Triggered when a bullet/projectile is successfully fired.
 */
public class BulletFiredEvent implements Event {
    public final Entity shooter;
    public final String soundPath; // Added for weapon-specific sound

    public BulletFiredEvent(Entity shooter) {
        this(shooter, null);
    }

    public BulletFiredEvent(Entity shooter, String soundPath) {
        this.shooter = shooter;
        this.soundPath = soundPath;
    }
}
