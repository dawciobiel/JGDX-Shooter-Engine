package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.events.HitEvent;

import java.util.List;

/**
 * Handles circular collision detection and triggers combat/pickup events.
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

                if (checkCollision(e1, e2)) {
                    handleCollision(e1, e2);
                }
            }
        }
    }

    private boolean checkCollision(Entity e1, Entity e2) {
        TransformComponent t1 = entityManager.getComponent(e1, TransformComponent.class);
        ColliderComponent c1 = entityManager.getComponent(e1, ColliderComponent.class);
        TransformComponent t2 = entityManager.getComponent(e2, TransformComponent.class);
        ColliderComponent c2 = entityManager.getComponent(e2, ColliderComponent.class);

        if (t1 == null || c1 == null || t2 == null || c2 == null) return false;

        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float distSq = dx * dx + dy * dy;
        float minDist = c1.radius + c2.radius;

        return distSq < minDist * minDist;
    }

    private void handleCollision(Entity e1, Entity e2) {
        // 1. Projectile Hits
        ProjectileComponent p1 = entityManager.getComponent(e1, ProjectileComponent.class);
        ProjectileComponent p2 = entityManager.getComponent(e2, ProjectileComponent.class);

        if (p1 != null) handleProjectileHit(e1, e2);
        if (p2 != null) handleProjectileHit(e2, e1);

        // 2. Direct Melee/Body Damage (Enemy touching Player)
        AIComponent ai1 = entityManager.getComponent(e1, AIComponent.class);
        PlayerComponent player2 = entityManager.getComponent(e2, PlayerComponent.class);
        if (ai1 != null && player2 != null) {
            triggerHit(e1, e2);
        }

        AIComponent ai2 = entityManager.getComponent(e2, AIComponent.class);
        PlayerComponent player1 = entityManager.getComponent(e1, PlayerComponent.class);
        if (ai2 != null && player1 != null) {
            triggerHit(e2, e1);
        }

        // 3. Pickups
        AmmoPickupComponent ammo1 = entityManager.getComponent(e1, AmmoPickupComponent.class);
        if (ammo1 != null && player2 != null) handleAmmoPickup(e2, e1, ammo1);

        AmmoPickupComponent ammo2 = entityManager.getComponent(e2, AmmoPickupComponent.class);
        if (player1 != null && ammo2 != null) handleAmmoPickup(e1, e2, ammo2);
    }

    private void handleProjectileHit(Entity projectile, Entity target) {
        ProjectileComponent pComp = entityManager.getComponent(projectile, ProjectileComponent.class);
        if (pComp == null || pComp.ownerId == target.getId()) return; // Don't hit self

        // Projectiles shouldn't hit other projectiles (optional)
        if (entityManager.hasComponent(target, ProjectileComponent.class)) return;

        triggerHit(projectile, target);
    }

    private void triggerHit(Entity attacker, Entity victim) {
        if (entityManager.hasComponent(victim, HealthComponent.class)) {
            eventBus.publish(new HitEvent(attacker, victim));
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
