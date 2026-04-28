package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as a player-controlled unit.
 */
public class PlayerComponent implements Component {
    public float speed = 150.0f;
    public String tauntsDir = null; // Directory containing random taunt sounds for this player
    public boolean invincible = false;
    public boolean infiniteAmmo = false;

    public PlayerComponent() {}
    public PlayerComponent(float speed) {
        this.speed = speed;
    }
}
