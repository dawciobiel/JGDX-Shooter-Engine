package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * High-level game rules. Loaded from assets/global/config/game.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameplayConfig {
    public boolean showUnitNames = true;
    public int multiKillThreshold = 3;
    public float multiKillWindow = 3.0f;
    public Difficulty difficulty = Difficulty.NORMAL;

    public enum Difficulty {
        EASY, NORMAL, HARD, NIGHTMARE
    }
}
