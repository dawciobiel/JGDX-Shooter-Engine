package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines an entity that can be picked up to gain ammunition.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmmoBoxPrefab {
    public String ammoPrefabPath; // Reference to which AmmoPrefab is inside
    public int quantity = 20;
    public String worldTexturePath; // Texture shown on the ground
    public String pickupSound;
}
