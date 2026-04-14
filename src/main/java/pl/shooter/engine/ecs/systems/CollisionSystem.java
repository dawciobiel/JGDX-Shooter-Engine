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
        updateProjectileCollisions();
        updatePickupCollisions();
    }

    private void updateProjectileCollisions() {
        List<Entity> projectiles = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class, ProjectileComponent.class);
        List<Entity> targets = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class, HealthComponent.class);

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

                if (tt == null || tc == null) continue;

                float dx = pt.x - tt.x;
                float dy = pt.y - tt.y;
                float distSq = dx * dx + dy * dy;
                float radiiSum = pc.radius + tc.radius;

                if (distSq < radiiSum * radiiSum) {
                    handleImpact(projectile, pp, target);
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

    private void updatePickupCollisions() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class, ColliderComponent.class);
        List<Entity> ammoPickups = entityManager.getEntitiesWithComponents(AmmoPickupComponent.class, TransformComponent.class, ColliderComponent.class);
        List<Entity> healthPickups = entityManager.getEntitiesWithComponents(HealthPickupComponent.class, TransformComponent.class, ColliderComponent.class);

        for (Entity player : players) {
            TransformComponent pt = entityManager.getComponent(player, TransformComponent.class);
            ColliderComponent pc = entityManager.getComponent(player, ColliderComponent.class);
            if (pt == null || pc == null) continue;

            // Handle Ammo Pickups
            for (Entity pickup : ammoPickups) {
                if (checkCollision(pt, pc, pickup)) {
                    handleAmmoPickup(player, pickup);
                    break; 
                }
            }

            // Handle Health Pickups
            for (Entity pickup : healthPickups) {
                if (checkCollision(pt, pc, pickup)) {
                    handleHealthPickup(player, pickup);
                    break;
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
        float distSq = dx * dx + dy * dy;
        float radiiSum = c1.radius + c2.radius;
        return distSq < radiiSum * radiiSum;
    }

    private void handleAmmoPickup(Entity player, Entity pickup) {
        AmmoPickupComponent ammo = entityManager.getComponent(pickup, AmmoPickupComponent.class);
        InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);
        
        if (inv != null && ammo != null) {
            // Fill current weapon and overall ammo
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

    private void handleImpact(Entity projectile, ProjectileComponent projComp, Entity target) {
        if (projComp.behavior == ProjectileComponent.Behavior.EXPLOSIVE) {
            createExplosionDamage(projectile, projComp);
        } else {
            eventBus.publish(new HitEvent(target, projComp.ownerId, projComp.damage));
        }

        TransformComponent pt = entityManager.getComponent(projectile, TransformComponent.class);
        if (pt != null && entityFactory != null && projComp.behavior != ProjectileComponent.Behavior.EXPLOSIVE) {
            entityFactory.createExplosion(pt.x, pt.y, Color.YELLOW);
        }
    }

    private void createExplosionDamage(Entity projectile, ProjectileComponent projComp) {
        TransformComponent pt = entityManager.getComponent(projectile, TransformComponent.class);
        if (pt == null) return;

        if (entityFactory != null) {
            entityFactory.createExplosion(pt.x, pt.y, Color.ORANGE);
        }

        List<Entity> targets = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class, HealthComponent.class);
        float radiusSq = projComp.explosionRadius * projComp.explosionRadius;

        for (Entity target : targets) {
            TransformComponent tt = entityManager.getComponent(target, TransformComponent.class);
            if (tt == null) continue;

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
