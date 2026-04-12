package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Tracks player stats: score and kill count.
 */
public class ScoreComponent implements Component {
    public int score = 0;
    public int kills = 0;
    public int wave = 1;

    public ScoreComponent() {}
}
