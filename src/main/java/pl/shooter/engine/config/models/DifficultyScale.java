package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Defines how roles (like ENEMY) map to specific CharacterPrefabs based on difficulty/level.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifficultyScale {
    public Map<Integer, String> enemyMapping; // Level -> CharacterPrefab path
    public Map<Integer, String> bossMapping;
}
