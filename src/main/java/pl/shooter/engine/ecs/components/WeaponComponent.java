package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores weapon data, supporting different firing patterns.
 */
public class WeaponComponent implements Component {
    public enum Type {
        PISTOL,
        SHOTGUN,
        MACHINE_GUN
    }

    public Type type = Type.PISTOL;
    public float fireRate;      // time between shots
    public float lastShotTime;
    public float projectileSpeed;
    public float spread;        // max angle deviation in degrees
    public int projectilesPerShot = 1;

    public WeaponComponent() {}
    public WeaponComponent(Type type, float fireRate, float projectileSpeed, float spread, int projectilesPerShot) {
        this.type = type;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.projectilesPerShot = projectilesPerShot;
        this.lastShotTime = 0;
    }
}
