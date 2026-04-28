package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ColliderComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Ensures smooth movement by sliding along obstacles instead of sticking.
 */
public class LocalAvoidanceSystem extends GameSystem {
    private final GameMap map;

    public LocalAvoidanceSystem(EntityManager entityManager, GameMap map) {
        super(entityManager);
        this.map = map;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> entities = entityManager.getEntitiesWithComponents(
                TransformComponent.class, VelocityComponent.class, ColliderComponent.class);

        for (Entity e : entities) {
            VelocityComponent v = entityManager.getComponent(e, VelocityComponent.class);
            if (v.vx == 0 && v.vy == 0) continue;

            TransformComponent t = entityManager.getComponent(e, TransformComponent.class);
            ColliderComponent c = entityManager.getComponent(e, ColliderComponent.class);

            float dx = v.vx * v.terrainMultiplier * deltaTime;
            float dy = v.vy * v.terrainMultiplier * deltaTime;

            // Slide logic: Try move X, if blocked try move Y
            if (!map.isWalkable(t.x + dx, t.y)) {
                v.vx = 0; // Stop X movement if blocked
            }
            if (!map.isWalkable(t.x, t.y + dy)) {
                v.vy = 0; // Stop Y movement if blocked
            }
        }
    }
}
