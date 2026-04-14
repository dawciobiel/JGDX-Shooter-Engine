package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores projectile-specific data.
 */
public class ProjectileComponent implements Component {
    public enum Behavior { NORMAL, EXPLOSIVE, PIERCING }

    public float lifetime; // how many seconds the projectile lasts
    public int ownerId;    // who shot this projectile
    public int damage;     // damage dealt on hit
    public Behavior behavior = Behavior.NORMAL;
    public float explosionRadius = 0f;

    // For piercing projectiles to hit each target only once
    public Set<Integer> hitEntities = new HashSet<>();

    public ProjectileComponent() {} // Required for JSON

    public ProjectileComponent(float lifetime, int ownerId, int damage) {
        this.lifetime = lifetime;
        this.ownerId = ownerId;
        this.damage = damage;
    }

    public ProjectileComponent(float lifetime, int ownerId, int damage, Behavior behavior, float explosionRadius) {
        this.lifetime = lifetime;
        this.ownerId = ownerId;
        this.damage = damage;
        this.behavior = behavior;
        this.explosionRadius = explosionRadius;
    }
}
