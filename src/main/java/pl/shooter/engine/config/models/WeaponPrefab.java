package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Defines a weapon template.
 * Weapons are now decoupled from specific projectiles, instead using Ammo Categories.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeaponPrefab {
    public String id;
    public String name;
    public List<String> allowedAmmoCategories; // e.g. ["9MM", "SPECIAL_9MM"]
    public Stats stats = new Stats();
    public Visuals visuals = new Visuals();
    public Audio audio = new Audio();

    public static class Stats {
        public float fireRate = 0.5f;
        public float spread = 2.0f;
        public int magazineSize = 10;
        public boolean isAutomatic = false;
        public float recoil = 1.0f;
    }

    public static class Visuals {
        public String iconPath;
        public String muzzleFlashParticle;
    }

    public static class Audio {
        public String shootSound;
        public String reloadSound;
        public String emptySound;
    }
}
