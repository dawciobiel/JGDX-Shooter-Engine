package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-level game rules. Loaded from assets/global/config/game.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameplayConfig {
    public boolean showUnitNames = true;
    public int multiKillThreshold = 3;
    public float multiKillWindow = 3.0f;
    public Difficulty difficulty = Difficulty.NORMAL;

    // Wave settings
    public float spawnInterval = 5.0f;
    public int maxEnemiesBase = 8;
    public int killsPerWave = 5;
    public List<String> enemyPool = new ArrayList<>(Arrays.asList("characters/zombie", "characters/zombie_fat")); //todo Wartości te powinny być ustawione w konfigu mapy, a nie na sztywno w kodzie całego silnika

    public enum Difficulty {
        EASY, NORMAL, HARD, NIGHTMARE
    }
}
