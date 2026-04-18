package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Event fired when an entity dies.
 */
public class DeathEvent implements Event {
    public final Entity victim;

    public DeathEvent(Entity victim) {
        this.victim = victim;
    }
}
