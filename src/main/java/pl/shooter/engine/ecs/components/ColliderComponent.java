package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.models.CharacterPrefab;
import pl.shooter.engine.config.models.ProjectilePrefab;

/**
 * Defines a circular collision body for an entity.
 * Can be initialized from CharacterPrefab or ProjectilePrefab.
 */
public class ColliderComponent implements Component {
    public float radius;

    public ColliderComponent() {} 
    public ColliderComponent(float radius) {
        this.radius = radius;
    }

    public ColliderComponent(CharacterPrefab.Stats stats) {
        this.radius = stats.radius;
    }

    public ColliderComponent(ProjectilePrefab prefab) {
        this.radius = prefab.radius;
    }
}
