package pl.shooter.events;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.events.Event;

/**
 * Triggered when a target is hit, either by a projectile or an explosion.
 */
public class HitEvent implements Event {
    public final Entity victim;
    public final int attackerId;
    public final int damage;

    public HitEvent(Entity victim, int attackerId, int damage) {
        this.victim = victim;
        this.attackerId = attackerId;
        this.damage = damage;
    }
}
