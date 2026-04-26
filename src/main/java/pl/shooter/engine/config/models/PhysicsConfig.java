package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Physics engine settings. Loaded from config/physics.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicsConfig {
    public float gravity = 0.0f;
    public float friction = 0.8f;
    public float restitution = 0.2f;
    public float pushForceMultiplier = 1.0f;
}
