package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Component;

public class RenderComponent implements Component {
    public Color color;
    public float radius;
    public boolean isCircle;

    public RenderComponent() {} // Required for JSON
    public RenderComponent(Color color, float radius, boolean isCircle) {
        this.color = color;
        this.radius = radius;
        this.isCircle = isCircle;
    }
}
