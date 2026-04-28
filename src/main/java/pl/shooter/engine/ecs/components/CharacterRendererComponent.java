package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.graphics.CharacterRenderer;

/**
 * Component that holds a character renderer strategy.
 */
public class CharacterRendererComponent implements Component {
    public CharacterRenderer renderer;

    public CharacterRendererComponent() {} 
    public CharacterRendererComponent(CharacterRenderer renderer) {
        this.renderer = renderer;
    }
}
