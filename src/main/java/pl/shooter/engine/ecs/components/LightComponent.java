package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Component;

public class LightComponent implements Component {
    public float radius;
    public Color color;
    public float intensity;
    public float z; // Height above the ground (for future pseudo-3D)

    public LightComponent() {
        this(150f, Color.WHITE, 1.0f);
    }

    public LightComponent(float radius, Color color, float intensity) {
        this.radius = radius;
        this.color = new Color(color);
        this.intensity = intensity;
        this.z = 0;
    }
}
