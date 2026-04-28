package pl.shooter.engine.graphics;

import pl.shooter.engine.config.models.CharacterPrefab;

/**
 * Factory for creating character renderers based on prefab configuration.
 */
public class CharacterRendererFactory {
    public static CharacterRenderer create(CharacterPrefab prefab) {
        if (prefab == null || prefab.visuals == null) {
            return new SpriteCharacterRenderer();
        }

        if ("procedural".equalsIgnoreCase(prefab.visuals.style)) {
            return new ProceduralCharacterRenderer(prefab.visuals.procedural);
        }

        // Default to sprite renderer
        return new SpriteCharacterRenderer();
    }
}
