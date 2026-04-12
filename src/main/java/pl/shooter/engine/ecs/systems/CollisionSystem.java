package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.ColliderComponent;
import pl.shooter.engine.ecs.components.ProjectileComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.events.HitEvent;

import java.util.List;

public class CollisionSystem extends GameSystem {
    private final EventBus eventBus;

    public CollisionSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> projectiles = entityManager.getEntitiesWithComponents(
                ProjectileComponent.class, TransformComponent.class, ColliderComponent.class
        );
        
        List<Entity> potentialVictims = entityManager.getEntitiesWithComponents(
                TransformComponent.class, ColliderComponent.class
        );

        for (Entity proj : projectiles) {
            // If projectile was removed in this frame by a previous collision
            if (!entityManager.getAllEntities().contains(proj)) continue;

            ProjectileComponent pData = entityManager.getComponent(proj, ProjectileComponent.class);
            TransformComponent pTrans = entityManager.getComponent(proj, TransformComponent.class);
            ColliderComponent pColl = entityManager.getComponent(proj, ColliderComponent.class);

            if (pData == null || pTrans == null || pColl == null) continue;

            for (Entity victim : potentialVictims) {
                // Check if victim still exists
                if (!entityManager.getAllEntities().contains(victim)) continue;
                
                // Don't collide with self or the shooter
                if (proj.equals(victim) || victim.getId() == pData.ownerId) {
                    continue;
                }

                TransformComponent vTrans = entityManager.getComponent(victim, TransformComponent.class);
                ColliderComponent vColl = entityManager.getComponent(victim, ColliderComponent.class);

                if (vTrans == null || vColl == null) continue;

                float distance = Vector2.dst(pTrans.x, pTrans.y, vTrans.x, vTrans.y);
                if (distance < pColl.radius + vColl.radius) {
                    eventBus.publish(new HitEvent(proj, victim));
                    // If the projectile hit something, it might be gone now
                    if (!entityManager.getAllEntities().contains(proj)) break;
                }
            }
        }
    }
}
