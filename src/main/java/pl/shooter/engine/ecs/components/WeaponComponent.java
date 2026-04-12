package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores weapon data, supporting different firing patterns and ammo.
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
    
    // Ammo system
    public int currentAmmo = 0;
    public int maxAmmo = 0;
    public boolean hasInfiniteAmmo = false;

    public WeaponComponent() {}
    public WeaponComponent(Type type, float fireRate, float projectileSpeed, float spread, int projectilesPerShot, int maxAmmo) {
        this.type = type;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.projectilesPerShot = projectilesPerShot;
        this.maxAmmo = maxAmmo;
        this.currentAmmo = maxAmmo;
        this.lastShotTime = 0;
        this.hasInfiniteAmmo = (type == Type.PISTOL); // Default Pistol to infinite
    }
}
