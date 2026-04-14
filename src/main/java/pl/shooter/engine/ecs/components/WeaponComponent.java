package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.WeaponConfig;

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

    public void applyConfig(WeaponConfig.WeaponData data) {
        if (data == null) return;
        this.fireRate = data.fireRate;
        this.projectileSpeed = data.projectileSpeed;
        this.spread = data.spread;
        this.projectilesPerShot = data.projectilesPerShot;
        this.maxAmmo = data.maxAmmo;
        this.currentAmmo = data.maxAmmo;
        this.magazineSize = data.magazineSize;
        this.magazineAmmo = data.magazineSize;
        this.reloadTime = data.reloadTime;
        this.hasInfiniteAmmo = data.hasInfiniteAmmo;
    }

    public static WeaponComponent create(Type type, WeaponConfig config) {
        WeaponComponent wc = new WeaponComponent();
        wc.type = type;
        if (config != null && config.weapons.containsKey(type.name())) {
            wc.applyConfig(config.weapons.get(type.name()));
        } else {
            // Fallback default
            wc.fireRate = 0.4f;
            wc.projectileSpeed = 450f;
            wc.spread = 3f;
            wc.magazineSize = 10;
            wc.magazineAmmo = 10;
            wc.hasInfiniteAmmo = (type == Type.PISTOL);
        }
        return wc;
    }
}
