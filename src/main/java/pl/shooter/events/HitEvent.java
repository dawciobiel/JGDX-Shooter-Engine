package pl.shooter.events;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.events.Event;

/**
 * Triggered when two colliders overlap.
 */
public class HitEvent implements Event {
    public final Entity attacker; // e.g. the projectile
    public final Entity victim;   // e.g. the enemy

    public HitEvent(Entity attacker, Entity victim) {
        this.attacker = attacker;
        this.victim = victim;
    }
}
