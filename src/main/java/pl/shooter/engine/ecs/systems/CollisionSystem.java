package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Handles physical collisions between entities.
 * Includes safety checks for entity lifecycle.
 */
public class CollisionSystem extends GameSystem {
    private final EventBus eventBus;
    private final EntityFactory entityFactory;

    public CollisionSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory) {
        super(entityManager);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> collidables = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class);
        
        for (int i = 0; i < collidables.size(); i++) {
            Entity e1 = collidables.get(i);
            // If e1 was removed by a previous collision in this frame, skip it
            if (!entityManager.isValid(e1)) continue;

            for (int j = i + 1; j < collidables.size(); j++) {
                Entity e2 = collidables.get(j);
                
                // If e2 was removed, skip it
                if (!entityManager.isValid(e2)) continue;

                handleCollision(e1, e2);

                // If e1 was removed during handleCollision (e.g. it was a projectile), 
                // we must stop checking it against other entities.
                if (!entityManager.isValid(e1)) break;
            }
        }
    }

    private void handleCollision(Entity e1, Entity e2) {
        TransformComponent t1 = entityManager.getComponent(e1, TransformComponent.class);
        ColliderComponent c1 = entityManager.getComponent(e1, ColliderComponent.class);
        TransformComponent t2 = entityManager.getComponent(e2, TransformComponent.class);
        ColliderComponent c2 = entityManager.getComponent(e2, ColliderComponent.class);

        // Double check components are still there
        if (t1 == null || c1 == null || t2 == null || c2 == null) return;

        float dist = Vector2.dst(t1.x, t1.y, t2.x, t2.y);
        float minDist = c1.radius + c2.radius;

        if (dist < minDist) {
            // --- Case A: Projectile hit ---
            if (entityManager.hasComponent(e1, ProjectileComponent.class)) {
                handleProjectileHit(e1, e2);
                return;
            }
            if (entityManager.hasComponent(e2, ProjectileComponent.class)) {
                handleProjectileHit(e2, e1);
                return;
            }

            // --- Case B: Ammo Pickup ---
            if (entityManager.hasComponent(e1, AmmoPickupComponent.class) && entityManager.hasComponent(e2, PlayerComponent.class)) {
                handleAmmoPickup(e1, e2);
                return;
            }
            if (entityManager.hasComponent(e2, AmmoPickupComponent.class) && entityManager.hasComponent(e1, PlayerComponent.class)) {
                handleAmmoPickup(e2, e1);
                return;
            }

            // --- Case C: Physical Push ---
            resolvePush(t1, c1, t2, c2);
        }
    }

    private void handleProjectileHit(Entity projectile, Entity victim) {
        ProjectileComponent pc = entityManager.getComponent(projectile, ProjectileComponent.class);
        if (pc == null || victim.getId() == pc.ownerId) return;

        eventBus.publish(new HitEvent(victim, pc.ownerId, pc.damage));
        entityManager.removeEntity(projectile);
    }

    private void handleAmmoPickup(Entity pickup, Entity player) {
        AmmoPickupComponent ammo = entityManager.getComponent(pickup, AmmoPickupComponent.class);
        InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);
        
        if (inv != null && ammo != null) {
            inv.addAmmo("9mm_regular", ammo.amount);
            entityManager.removeEntity(pickup);
        }
    }

    private void resolvePush(TransformComponent t1, ColliderComponent c1, TransformComponent t2, ColliderComponent c2) {
        Vector2 diff = new Vector2(t1.x - t2.x, t1.y - t2.y);
        float dist = diff.len();
        if (dist == 0) return;

        float overlap = (c1.radius + c2.radius) - dist;
        diff.nor().scl(overlap / 2f);
        
        t1.x += diff.x;
        t1.y += diff.y;
        t2.x -= diff.x;
        t2.y -= diff.y;
    }
}
