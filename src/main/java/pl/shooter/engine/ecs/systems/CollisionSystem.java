package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Handles basic circular collision detection between entities.
 */
public class CollisionSystem extends GameSystem {
    private final EventBus eventBus;

    public CollisionSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> collidables = entityManager.getEntitiesWithComponents(ColliderComponent.class, TransformComponent.class);

        for (int i = 0; i < collidables.size(); i++) {
            for (int j = i + 1; j < collidables.size(); j++) {
                Entity e1 = collidables.get(i);
                Entity e2 = collidables.get(j);

                checkCollision(e1, e2);
            }
        }
    }

    private void checkCollision(Entity e1, Entity e2) {
        TransformComponent t1 = entityManager.getComponent(e1, TransformComponent.class);
        ColliderComponent c1 = entityManager.getComponent(e1, ColliderComponent.class);
        TransformComponent t2 = entityManager.getComponent(e2, TransformComponent.class);
        ColliderComponent c2 = entityManager.getComponent(e2, ColliderComponent.class);

        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float distSq = dx * dx + dy * dy;
        float minDist = c1.radius + c2.radius;

        if (distSq < minDist * minDist) {
            handleCollision(e1, e2);
        }
    }

    private void handleCollision(Entity e1, Entity e2) {
        // Projectile collisions
        ProjectileComponent p1 = entityManager.getComponent(e1, ProjectileComponent.class);
        ProjectileComponent p2 = entityManager.getComponent(e2, ProjectileComponent.class);

        if (p1 != null) handleProjectileHit(e1, e2);
        else if (p2 != null) handleProjectileHit(e2, e1);

        // Player vs Ammo Pickup collisions
        PlayerComponent player1 = entityManager.getComponent(e1, PlayerComponent.class);
        AmmoPickupComponent ammo1 = entityManager.getComponent(e1, AmmoPickupComponent.class);
        PlayerComponent player2 = entityManager.getComponent(e2, PlayerComponent.class);
        AmmoPickupComponent ammo2 = entityManager.getComponent(e2, AmmoPickupComponent.class);

        if (player1 != null && ammo2 != null) handleAmmoPickup(e1, e2, ammo2);
        else if (player2 != null && ammo1 != null) handleAmmoPickup(e2, e1, ammo1);
    }

    private void handleProjectileHit(Entity projectile, Entity target) {
        ProjectileComponent pComp = entityManager.getComponent(projectile, ProjectileComponent.class);
        if (pComp != null && pComp.ownerId == target.getId()) return; // Don't hit self

        HealthComponent health = entityManager.getComponent(target, HealthComponent.class);
        if (health != null) {
            eventBus.publish(new HitEvent(projectile, target));
        }
    }

    private void handleAmmoPickup(Entity player, Entity pickup, AmmoPickupComponent ammoComp) {
        WeaponComponent weapon = entityManager.getComponent(player, WeaponComponent.class);
        if (weapon != null) {
            weapon.currentAmmo = Math.min(weapon.maxAmmo, weapon.currentAmmo + ammoComp.amount);
            entityManager.removeEntity(pickup);
        }
    }
}
