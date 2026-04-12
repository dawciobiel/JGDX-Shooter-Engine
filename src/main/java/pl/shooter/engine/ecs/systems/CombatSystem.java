package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

/**
 * Handles combat logic, including weapon types, firing patterns, ammo consumption, and reloading.
 */
public class CombatSystem extends GameSystem {
    private float totalTime = 0;

    public CombatSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        eventBus.subscribe(ShootEvent.class, this::handleShoot);
    }

    @Override
    public void update(float deltaTime) {
        totalTime += deltaTime;

        // Process reloading
        for (Entity entity : entityManager.getEntitiesWithComponents(WeaponComponent.class)) {
            WeaponComponent weapon = entityManager.getComponent(entity, WeaponComponent.class);
            if (weapon == null) continue;

            if (weapon.isReloading) {
                weapon.reloadTimer += deltaTime;
                if (weapon.reloadTimer >= weapon.reloadTime) {
                    finishReload(weapon);
                }
            } else if (weapon.magazineSize > 0 && weapon.magazineAmmo <= 0 && (weapon.currentAmmo > 0 || weapon.hasInfiniteAmmo)) {
                // Auto-reload if empty, ammo available, and magazine size is defined
                startReload(weapon);
            }
        }
    }

    private void handleShoot(ShootEvent event) {
        Entity shooter = event.shooter;
        WeaponComponent weapon = entityManager.getComponent(shooter, WeaponComponent.class);
        TransformComponent shooterTransform = entityManager.getComponent(shooter, TransformComponent.class);

        if (weapon == null || shooterTransform == null) return;

        // 1. Reloading check
        if (weapon.isReloading) {
            return;
        }

        // 2. Cooldown check
        if (totalTime - weapon.lastShotTime < weapon.fireRate) {
            return;
        }

        // 3. Magazine check (Skip if magazineSize is 0 - treat as non-mag weapon)
        if (weapon.magazineSize > 0 && weapon.magazineAmmo <= 0) {
            startReload(weapon);
            return;
        }

        // 4. Firing logic
        weapon.lastShotTime = totalTime;
        if (weapon.magazineSize > 0) {
            weapon.magazineAmmo--;
        }

        // Base angle to target
        float baseAngle = MathUtils.atan2(event.targetY - shooterTransform.y, event.targetX - shooterTransform.x) * MathUtils.radiansToDegrees;

        // Fire multiple projectiles if weapon allows (e.g. Shotgun)
        for (int i = 0; i < weapon.projectilesPerShot; i++) {
            float finalAngle = baseAngle;
            if (weapon.spread > 0) {
                finalAngle += MathUtils.random(-weapon.spread, weapon.spread);
            }
            spawnProjectile(shooterTransform.x, shooterTransform.y, finalAngle, weapon.projectileSpeed, shooter.getId());
        }
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

    private void spawnProjectile(float x, float y, float angle, float speed, int ownerId) {
        Entity projectile = entityManager.createEntity();
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        float vx = MathUtils.cosDeg(angle) * speed;
        float vy = MathUtils.sinDeg(angle) * speed;
        entityManager.addComponent(projectile, new VelocityComponent(vx, vy));
        entityManager.addComponent(projectile, new RenderComponent(Color.YELLOW, 3f, true));
        entityManager.addComponent(projectile, new ProjectileComponent(1.5f, ownerId));
        entityManager.addComponent(projectile, new ColliderComponent(3f));
    }
}
