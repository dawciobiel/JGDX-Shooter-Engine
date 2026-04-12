package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores the reference to a texture asset.
 */
public class TextureComponent implements Component {
    public String assetPath;
    public float width;
    public float height;

    public TextureComponent() {} // Required for JSON
    public TextureComponent(String assetPath, float width, float height) {
        this.assetPath = assetPath;
        this.width = width;
        this.height = height;
    }
}
