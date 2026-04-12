package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores weapon data, supporting firing patterns, ammo, and reloading.
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
    public int currentAmmo = 0;      // Total ammo (outside magazine)
    public int maxAmmo = 0;          // Max total ammo
    public boolean hasInfiniteAmmo = false;

    // Reloading system
    public int magazineAmmo = 0;     // Ammo currently in magazine
    public int magazineSize = 0;     // Max ammo in magazine
    public float reloadTime = 1.5f;  // Seconds to reload
    public float reloadTimer = 0;    // Current reload progress
    public boolean isReloading = false;

    public WeaponComponent() {}
    public WeaponComponent(Type type, float fireRate, float projectileSpeed, float spread, int projectilesPerShot, int totalAmmo, int magSize, float reloadTime) {
        this.type = type;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.projectilesPerShot = projectilesPerShot;
        
        this.maxAmmo = totalAmmo;
        this.currentAmmo = totalAmmo;
        this.magazineSize = magSize;
        this.magazineAmmo = magSize;
        this.reloadTime = reloadTime;

        this.lastShotTime = 0;
        this.hasInfiniteAmmo = (type == Type.PISTOL);
    }
}
