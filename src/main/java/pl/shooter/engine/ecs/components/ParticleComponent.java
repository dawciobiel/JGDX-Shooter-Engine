package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Data for visual effects particles.
 */
public class ParticleComponent implements Component {
    public float alpha = 1.0f;
    public float fadeSpeed; // how fast it disappears (e.g. 0.5 per second)
    public float scaleSpeed; // how fast it shrinks

    public ParticleComponent() {}
    public ParticleComponent(float fadeSpeed, float scaleSpeed) {
        this.fadeSpeed = fadeSpeed;
        this.scaleSpeed = scaleSpeed;
    }
}
