package pl.shooter.engine.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration data for all weapon types, loaded from JSON.
 */
public class WeaponConfig {
    public Map<String, WeaponData> weapons = new HashMap<>();

    public static class WeaponData {
        public float fireRate;
        public float projectileSpeed;
        public float spread;
        public int projectilesPerShot = 1;
        public int maxAmmo;
        public int magazineSize;
        public float reloadTime;
        public boolean hasInfiniteAmmo;
        public float range = 50f; // Added for melee weapons
        public ProjectileData projectile;
    }

    public static class ProjectileData {
        public String color = "YELLOW"; // Stores color name for simplified JSON
        public float radius = 3.0f;
        public float lifetime = 1.5f;
        public int damage = 10;
        public String behavior = "NORMAL"; // NORMAL, EXPLOSIVE, PIERCING
        public float explosionRadius = 0f;
    }
}
