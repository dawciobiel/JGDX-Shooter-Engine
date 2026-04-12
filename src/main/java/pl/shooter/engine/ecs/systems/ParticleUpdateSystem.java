package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ParticleComponent;
import pl.shooter.engine.ecs.components.RenderComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates particle effects and removes them when they fade out.
 */
public class ParticleUpdateSystem extends GameSystem {

    public ParticleUpdateSystem(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> particles = entityManager.getEntitiesWithComponents(ParticleComponent.class, RenderComponent.class);
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : particles) {
            ParticleComponent p = entityManager.getComponent(entity, ParticleComponent.class);
            RenderComponent r = entityManager.getComponent(entity, RenderComponent.class);

            p.alpha -= p.fadeSpeed * deltaTime;
            r.radius -= p.scaleSpeed * deltaTime;

            // Apply alpha to color
            r.color.a = Math.max(0, p.alpha);

            if (p.alpha <= 0 || r.radius <= 0) {
                toRemove.add(entity);
            }
        }

        for (Entity entity : toRemove) {
            entityManager.removeEntity(entity);
        }
    }
}
