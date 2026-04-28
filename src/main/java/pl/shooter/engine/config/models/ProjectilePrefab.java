package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Defines the physical and visual properties of a projectile in the game world.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectilePrefab {
    public String name;
    public float baseDamage = 10f;
    public float speed = 600f;
    public float radius = 4f; // Collision radius
    public float visualWidth = 0;  // 0 means use radius*2
    public float visualHeight = 0; // 0 means use radius*2
    public String texturePath;
    public ExplosionData explosion; // Optional area of effect data

    public static class ExplosionData {
        public float radius = 50f;
        public float areaDamage = 50f;
        public float pushForce = 300f;
        public String particleEffect; // Visual effect to trigger
        public String soundEffect;    // Sound to play on explosion
        public boolean destroysTerrain = false; // For future destructible environment
    }
}
