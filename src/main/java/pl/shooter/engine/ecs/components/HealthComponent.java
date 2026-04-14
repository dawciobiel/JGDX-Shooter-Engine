package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Component;

/**
 * Stores health data and corpse/blood settings for entities.
 */
public class HealthComponent implements Component {
    public float hp;
    public float maxHp;
    
    // Corpse system
    public boolean isDead = false;
    public float deathTimer = 0f;
    public float corpseDuration = 3.0f; // Default 3 seconds
    public boolean corpseStayPermanent = false; // If true, corpse/blood doesn't fade
    
    // Blood system
    public boolean hasBlood = true;
    public Color bloodColor = Color.RED;
    public float bloodSize = 25f; // Radius of the blood puddle

    public HealthComponent() {} // Required for JSON
    public HealthComponent(float hp) {
        this.hp = hp;
        this.maxHp = hp;
    }
    public HealthComponent(float hp, float maxHp) {
        this.hp = hp;
        this.maxHp = maxHp;
    }
}
