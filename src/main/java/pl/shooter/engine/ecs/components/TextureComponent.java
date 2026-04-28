package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.models.CharacterPrefab;
import pl.shooter.engine.config.models.ProjectilePrefab;

/**
 * Stores the reference to a texture asset.
 */
public class TextureComponent implements Component {
    public String assetPath;
    public float width;
    public float height;

    public TextureComponent() {} 
    public TextureComponent(String assetPath, float width, float height) {
        this.assetPath = assetPath;
        this.width = width;
        this.height = height;
    }

    public TextureComponent(CharacterPrefab prefab) {
        this.assetPath = prefab.visuals.texturePath;
        this.width = prefab.visuals.frameWidth;
        this.height = prefab.visuals.frameHeight;
    }

    public TextureComponent(ProjectilePrefab prefab) {
        this.assetPath = prefab.texturePath;
        // Use custom visual size if defined, otherwise fallback to radius
        this.width = prefab.visualWidth > 0 ? prefab.visualWidth : prefab.radius * 2;
        this.height = prefab.visualHeight > 0 ? prefab.visualHeight : prefab.radius * 2;
    }
}
