package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines a specific type of ammunition resource.
 * Connects the resource in inventory with the projectile it produces.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmmoPrefab {
    public String id; // unique ID like "9mm_regular"
    public String name;
    public String category; // category like "9MM", "SHELLS", "ROCKETS"
    public String projectilePrefabPath; // Reference to a ProjectilePrefab
    public int projectilesPerUnit = 1; // 1 for bullets, 8+ for shotgun shells
    public float spreadMultiplier = 1.0f; // Some ammo might be less accurate
    public String iconPath;
}
