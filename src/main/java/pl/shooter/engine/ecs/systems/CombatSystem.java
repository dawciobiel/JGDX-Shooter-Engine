package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.BulletFiredEvent;
import pl.shooter.engine.events.EmptyWeaponEvent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

/**
 * Handles combat logic, including weapon types, firing patterns, ammo consumption, and reloading.
 */
public class CombatSystem extends GameSystem {
    private final EventBus eventBus;
    private final EntityFactory entityFactory;
    private float totalTime = 0;

    public CombatSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory) {
        super(entityManager);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        eventBus.subscribe(ShootEvent.class, this::handleShoot);
    }

    @Override
    public void update(float deltaTime) {
        totalTime += deltaTime;

        // Process reloading for all weapons in all inventories
        for (Entity entity : entityManager.getEntitiesWithComponents(InventoryComponent.class)) {
            InventoryComponent inv = entityManager.getComponent(entity, InventoryComponent.class);
            for (WeaponComponent weapon : inv.weapons) {
                if (weapon.isReloading) {
                    weapon.reloadTimer += deltaTime;
                    if (weapon.reloadTimer >= weapon.reloadTime) {
                        finishReload(weapon);
                    }
                } else if (weapon.magazineSize > 0 && weapon.magazineAmmo <= 0 && (weapon.currentAmmo > 0 || weapon.hasInfiniteAmmo)) {
                    startReload(weapon);
                }
            }
        }
    }

    private void handleShoot(ShootEvent event) {
        Entity shooter = event.shooter;
        WeaponComponent weapon = entityManager.getComponent(shooter, WeaponComponent.class);
        TransformComponent shooterTransform = entityManager.getComponent(shooter, TransformComponent.class);

        if (weapon == null || shooterTransform == null) return;

        if (weapon.isReloading) return;

        if (totalTime - weapon.lastShotTime < weapon.fireRate) return;

        if (weapon.magazineSize > 0 && weapon.magazineAmmo <= 0) {
            if (weapon.currentAmmo <= 0 && !weapon.hasInfiniteAmmo) {
                eventBus.publish(new EmptyWeaponEvent(shooter));
                weapon.lastShotTime = totalTime; 
            } else {
                startReload(weapon);
            }
            return;
        }

        weapon.lastShotTime = totalTime;
        if (weapon.magazineSize > 0) {
            weapon.magazineAmmo--;
        }

        eventBus.publish(new BulletFiredEvent(shooter));

        float baseAngle = MathUtils.atan2(event.targetY - shooterTransform.y, event.targetX - shooterTransform.x) * MathUtils.radiansToDegrees;

        // Shell ejection (only for bullet-based weapons)
        if (entityFactory != null && isBallistic(weapon.type)) {
            entityFactory.createShellEjection(shooterTransform.x, shooterTransform.y, baseAngle);
        }

        for (int i = 0; i < weapon.projectilesPerShot; i++) {
            float finalAngle = baseAngle;
            if (weapon.spread > 0) {
                finalAngle += MathUtils.random(-weapon.spread, weapon.spread);
            }
            spawnProjectileByType(shooterTransform.x, shooterTransform.y, finalAngle, weapon, shooter.getId());
        }
    }

    private boolean isBallistic(WeaponComponent.Type type) {
        return type == WeaponComponent.Type.PISTOL || type == WeaponComponent.Type.SHOTGUN || 
               type == WeaponComponent.Type.MACHINE_GUN || type == WeaponComponent.Type.SNIPER_RIFLE;
    }

    private void spawnProjectileByType(float x, float y, float angle, WeaponComponent weapon, int ownerId) {
        Entity projectile = entityManager.createEntity();
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        
        float vx = MathUtils.cosDeg(angle) * weapon.projectileSpeed;
        float vy = MathUtils.sinDeg(angle) * weapon.projectileSpeed;
        entityManager.addComponent(projectile, new VelocityComponent(vx, vy));
        
        Color color = Color.YELLOW;
        float radius = 3f;
        float lifetime = 1.5f;

        switch (weapon.type) {
            case SHOTGUN: color = Color.LIGHT_GRAY; radius = 2.5f; lifetime = 0.5f; break;
            case SNIPER_RIFLE: color = Color.WHITE; radius = 2f; lifetime = 3.0f; break;
            case MACHINE_GUN: color = Color.ORANGE; radius = 2.5f; break;
            case PLASMA_GUN: color = Color.CYAN; radius = 6f; lifetime = 2.0f; break;
            case ROCKET_LAUNCHER: color = Color.RED; radius = 8f; lifetime = 4.0f; break;
            case LIGHTNING_GUN: color = Color.BLUE; radius = 2f; lifetime = 0.8f; break;
            case RAIL_GUN: color = Color.PURPLE; radius = 4f; lifetime = 5.0f; break;
            case GRENADE: color = Color.FOREST; radius = 5f; lifetime = 2.5f; break;
        }

        entityManager.addComponent(projectile, new RenderComponent(color, radius, true));
        entityManager.addComponent(projectile, new ProjectileComponent(lifetime, ownerId));
        entityManager.addComponent(projectile, new ColliderComponent(radius));
    }

    private void startReload(WeaponComponent weapon) {
        if (!weapon.isReloading && (weapon.currentAmmo > 0 || weapon.hasInfiniteAmmo)) {
            weapon.isReloading = true;
            weapon.reloadTimer = 0;
        }
    }

    private void finishReload(WeaponComponent weapon) {
        if (!weapon.isReloading) return;
        int needed = weapon.magazineSize - weapon.magazineAmmo;
        if (weapon.hasInfiniteAmmo) {
            weapon.magazineAmmo = weapon.magazineSize;
        } else {
            int toTransfer = Math.min(needed, weapon.currentAmmo);
            weapon.currentAmmo -= toTransfer;
            weapon.magazineAmmo += toTransfer;
        }
        weapon.isReloading = false;
        weapon.reloadTimer = 0;
    }
}
