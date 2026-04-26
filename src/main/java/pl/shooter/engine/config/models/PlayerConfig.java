package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the player's specific configuration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerConfig {
    public String nickname = "Player 1";
    public String characterPrefab = "characters/soldier";
    
    // Starting equipment
    public List<String> startingWeapons = new ArrayList<>(); // Paths to WeaponPrefabs
    public Map<String, Integer> startingAmmo = new HashMap<>(); // AmmoPrefab.id -> quantity

    public float initialScore = 0;
    public boolean invincible = false;
    public boolean infiniteAmmo = false;
}
