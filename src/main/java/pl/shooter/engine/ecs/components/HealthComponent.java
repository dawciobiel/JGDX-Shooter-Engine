package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.models.CharacterPrefab;

/**
 * Stores health data and corpse/blood settings for entities.
 * Can be initialized from a CharacterPrefab.
 */
public class HealthComponent implements Component {
    public float hp;
    public float maxHp;
    
    public boolean isDead = false;
    public float deathTimer = 0f;
    public float corpseDuration = 3.0f;
    public boolean corpseStayPermanent = false;
    
    public boolean hasBlood = true;
    public Color bloodColor = new Color(0.8f, 0, 0, 1); // Professional dark red
    public float bloodSize = 25f;

    public HealthComponent() {} 

    public HealthComponent(float hp) {
        this.hp = hp;
        this.maxHp = hp;
    }

    /**
     * Initializes component using data from the CharacterPrefab.
     */
    public HealthComponent(CharacterPrefab.Stats stats) {
        this.hp = stats.health;
        this.maxHp = stats.health;
    }
}
