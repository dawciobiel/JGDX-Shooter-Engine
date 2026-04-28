package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.models.WeaponPrefab;
import pl.shooter.engine.config.models.AmmoPrefab;

import java.util.List;

/**
 * Stores operational weapon data. 
 * Linked to a WeaponPrefab and manages its own magazine/reloading state.
 */
public class WeaponComponent implements Component {
    public String prefabId;
    public String name;
    
    // Operational stats
    public float fireRate;      
    public float lastShotTime;
    public float spread;        
    public int magazineAmmo;     
    public int magazineSize;     
    public float reloadTime = 1.5f;  
    public float reloadTimer = 0;    
    public boolean isReloading = false;
    public boolean isAutomatic = false;
    
    // Decoupled Ammo Info
    public List<String> allowedAmmoCategories;
    public AmmoPrefab activeAmmo; // The ammo type currently being used

    // Visuals & Audio from prefab
    public String iconPath;
    public String shootSound;

    public WeaponComponent() {}

    /**
     * Initializes component from a WeaponPrefab.
     */
    public WeaponComponent(WeaponPrefab prefab) {
        this.prefabId = prefab.id;
        this.name = prefab.name;
        this.fireRate = prefab.stats.fireRate;
        this.spread = prefab.stats.spread;
        this.magazineSize = prefab.stats.magazineSize;
        this.magazineAmmo = prefab.stats.magazineSize;
        this.isAutomatic = prefab.stats.isAutomatic;
        this.allowedAmmoCategories = prefab.allowedAmmoCategories;
        
        this.iconPath = prefab.visuals.iconPath;
        this.shootSound = prefab.audio.shootSound;
    }
}
