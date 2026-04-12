package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ProjectileComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of projectiles.
 * Removes projectiles after their lifetime expires.
 */
public class ProjectileSystem extends GameSystem {

    public ProjectileSystem(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> projectiles = entityManager.getEntitiesWithComponents(ProjectileComponent.class);
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : projectiles) {
            ProjectileComponent projectile = entityManager.getComponent(entity, ProjectileComponent.class);
            projectile.lifetime -= deltaTime;

            if (projectile.lifetime <= 0) {
                toRemove.add(entity);
            }
        }

        // Cleanup expired projectiles
        for (Entity entity : toRemove) {
            entityManager.removeEntity(entity);
        }
    }
}
