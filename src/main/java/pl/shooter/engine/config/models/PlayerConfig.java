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
    public String nickname = "Unknown Player";
    public String characterPrefab = null;
    
    // Starting equipment
    public List<String> startingWeapons = new ArrayList<>(); 
    public Map<String, Integer> startingAmmo = new HashMap<>(); 

    public float initialScore = 0;
    public boolean invincible = false;
    public boolean infiniteAmmo = false;
}
