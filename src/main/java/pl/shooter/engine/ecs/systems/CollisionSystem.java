package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.events.HitEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.PickupEvent;

import java.util.List;

/**
 * Handles collisions between projectiles, pickups, and entities.
 * Optimized for reduced ECS lookups and faster intersection tests.
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
        // Pre-fetch commonly used entity lists
        List<Entity> targets = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class, HealthComponent.class);
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class, ColliderComponent.class);
        
        updateProjectileCollisions(targets);
        updatePickupCollisions(players);
    }

    private void updateProjectileCollisions(List<Entity> targets) {
        List<Entity> projectiles = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class, ProjectileComponent.class);

        for (Entity projectile : projectiles) {
            TransformComponent pt = entityManager.getComponent(projectile, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(projectile, ColliderComponent.class);
            ProjectileComponent pp = entityManager.getComponent(projectile, ProjectileComponent.class);

            if (pt == null || pc == null || pp == null) continue;

            boolean destroyed = false;
            for (Entity target : targets) {
                if (target.getId() == pp.ownerId) continue;
                if (pp.behavior == ProjectileComponent.Behavior.PIERCING && pp.hitEntities.contains(target.getId())) continue;

                TransformComponent tt = entityManager.getComponent(target, TransformComponent.class);
                ColliderComponent tc = entityManager.getComponent(target, ColliderComponent.class);

                // SAFETY CHECK: Component might have been removed since list was fetched
                if (tt == null || tc == null) continue;

                float dx = pt.x - tt.x;
                float dy = pt.y - tt.y;
                float radiiSum = pc.radius + tc.radius;
                
                if (Math.abs(dx) > radiiSum || Math.abs(dy) > radiiSum) continue;

                if ((dx * dx + dy * dy) < (radiiSum * radiiSum)) {
                    handleImpact(projectile, pp, target, targets);
                    if (pp.behavior != ProjectileComponent.Behavior.PIERCING) {
                        destroyed = true;
                        break;
                    } else {
                        pp.hitEntities.add(target.getId());
                    }
                }
            }
            if (destroyed) {
                entityManager.removeEntity(projectile);
            }
        }
    }

    private void updatePickupCollisions(List<Entity> players) {
        if (players.isEmpty()) return;

        List<Entity> ammoPickups = entityManager.getEntitiesWithComponents(AmmoPickupComponent.class, TransformComponent.class, ColliderComponent.class);
        List<Entity> healthPickups = entityManager.getEntitiesWithComponents(HealthPickupComponent.class, TransformComponent.class, ColliderComponent.class);

        for (Entity player : players) {
            TransformComponent pt = entityManager.getComponent(player, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(player, ColliderComponent.class);

            for (Entity pickup : ammoPickups) {
                if (checkCollision(pt, pc, pickup)) {
                    handleAmmoPickup(player, pickup);
                }
            }

            for (Entity pickup : healthPickups) {
                if (checkCollision(pt, pc, pickup)) {
                    handleHealthPickup(player, pickup);
                }
            }
        }
    }

    private boolean checkCollision(TransformComponent t1, ColliderComponent c1, Entity e2) {
        TransformComponent t2 = entityManager.getComponent(e2, TransformComponent.class);
        ColliderComponent c2 = entityManager.getComponent(e2, ColliderComponent.class);
        if (t2 == null || c2 == null) return false;

        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float radiiSum = c1.radius + c2.radius;
        
        if (Math.abs(dx) > radiiSum || Math.abs(dy) > radiiSum) return false;
        return (dx * dx + dy * dy) < (radiiSum * radiiSum);
    }

    private void handleAmmoPickup(Entity player, Entity pickup) {
        AmmoPickupComponent ammo = entityManager.getComponent(pickup, AmmoPickupComponent.class);
        InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);
        if (inv != null && ammo != null) {
            for (WeaponComponent weapon : inv.weapons) {
                if (!weapon.hasInfiniteAmmo) {
                    weapon.currentAmmo = Math.min(weapon.currentAmmo + ammo.amount, weapon.maxAmmo);
                }
            }
            eventBus.publish(new PickupEvent(player));
            entityManager.removeEntity(pickup);
        }
    }

    private void handleHealthPickup(Entity player, Entity pickup) {
        HealthPickupComponent hp = entityManager.getComponent(pickup, HealthPickupComponent.class);
        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
        if (health != null && hp != null) {
            health.hp = Math.min(health.hp + hp.amount, health.maxHp);
            eventBus.publish(new PickupEvent(player));
            entityManager.removeEntity(pickup);
        }
    }

    private void handleImpact(Entity projectile, ProjectileComponent projComp, Entity target, List<Entity> targets) {
        if (projComp.behavior == ProjectileComponent.Behavior.EXPLOSIVE) {
            createExplosionDamage(projectile, projComp, targets);
        } else {
            eventBus.publish(new HitEvent(target, projComp.ownerId, projComp.damage));
        }

        TransformComponent pt = entityManager.getComponent(projectile, TransformComponent.class);
        if (pt != null && entityFactory != null && projComp.behavior != ProjectileComponent.Behavior.EXPLOSIVE) {
            entityFactory.createExplosion(pt.x, pt.y, Color.YELLOW);
        }
    }

    private void createExplosionDamage(Entity projectile, ProjectileComponent projComp, List<Entity> targets) {
        TransformComponent pt = entityManager.getComponent(projectile, TransformComponent.class);
        if (pt == null) return;

        if (entityFactory != null) {
            entityFactory.createExplosion(pt.x, pt.y, Color.ORANGE);
        }

        float radiusSq = projComp.explosionRadius * projComp.explosionRadius;
        for (Entity target : targets) {
            TransformComponent tt = entityManager.getComponent(target, TransformComponent.class);
            if (tt == null) continue; // Target might have been destroyed by previous explosion in same frame

            float dx = pt.x - tt.x;
            float dy = pt.y - tt.y;
            float distSq = dx * dx + dy * dy;

            if (distSq < radiusSq) {
                float distance = (float) Math.sqrt(distSq);
                float falloff = 1.0f - (distance / projComp.explosionRadius);
                int finalDamage = (int) (projComp.damage * falloff);
                if (finalDamage > 0) {
                    eventBus.publish(new HitEvent(target, projComp.ownerId, finalDamage));
                }
            }
        }
    }
}
