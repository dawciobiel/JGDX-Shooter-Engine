package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores weapon data, supporting firing patterns, ammo, and reloading.
 */
public class WeaponComponent implements Component {
    public enum Type {
        PISTOL,
        SHOTGUN,
        MACHINE_GUN,
        SNIPER_RIFLE,
        PLASMA_GUN,
        ROCKET_LAUNCHER,
        LIGHTNING_GUN,
        RAIL_GUN,
        GRENADE
    }

    public Type type = Type.PISTOL;
    public float fireRate;      
    public float lastShotTime;
    public float projectileSpeed;
    public float spread;        
    public int projectilesPerShot = 1;
    
    // Ammo system
    public int currentAmmo = 0;      
    public int maxAmmo = 0;          
    public boolean hasInfiniteAmmo = false;

    // Reloading system
    public int magazineAmmo = 0;     
    public int magazineSize = 0;     
    public float reloadTime = 1.5f;  
    public float reloadTimer = 0;    
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

    public static WeaponComponent create(Type type) {
        switch (type) {
            case SHOTGUN:
                return new WeaponComponent(Type.SHOTGUN, 0.8f, 400f, 15f, 6, 40, 2, 2.5f);
            case MACHINE_GUN:
                return new WeaponComponent(Type.MACHINE_GUN, 0.1f, 500f, 5f, 1, 200, 30, 2.0f);
            case SNIPER_RIFLE:
                return new WeaponComponent(Type.SNIPER_RIFLE, 1.5f, 1200f, 0f, 1, 20, 5, 3.5f);
            case PLASMA_GUN:
                return new WeaponComponent(Type.PLASMA_GUN, 0.3f, 350f, 2f, 1, 60, 15, 1.8f);
            case ROCKET_LAUNCHER:
                return new WeaponComponent(Type.ROCKET_LAUNCHER, 1.2f, 300f, 2f, 1, 10, 1, 3.0f);
            case LIGHTNING_GUN:
                return new WeaponComponent(Type.LIGHTNING_GUN, 0.05f, 800f, 1f, 1, 100, 100, 4.0f);
            case RAIL_GUN:
                return new WeaponComponent(Type.RAIL_GUN, 2.0f, 2000f, 0f, 1, 10, 1, 4.0f);
            case GRENADE:
                return new WeaponComponent(Type.GRENADE, 1.0f, 250f, 10f, 1, 15, 1, 2.0f);
            default:
                return new WeaponComponent(Type.PISTOL, 0.4f, 450f, 3f, 1, 0, 10, 1.2f);
        }
    }
}
