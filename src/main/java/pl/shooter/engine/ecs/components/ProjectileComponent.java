package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores projectile-specific data.
 */
public class ProjectileComponent implements Component {
    public float lifetime; // how many seconds the projectile lasts
    public int ownerId;    // who shot this projectile

    public ProjectileComponent(float lifetime, int ownerId) {
        this.lifetime = lifetime;
        this.ownerId = ownerId;
    }
}
