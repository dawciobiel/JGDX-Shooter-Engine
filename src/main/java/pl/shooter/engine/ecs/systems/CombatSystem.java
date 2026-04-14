package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.WeaponConfig;
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
    private final ConfigService configService;
    private float totalTime = 0;

    public CombatSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory) {
        this(entityManager, eventBus, entityFactory, new ConfigService());
    }

    public CombatSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory, ConfigService configService) {
        super(entityManager);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        this.configService = configService;
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

        // Get projectile data from config if available
        WeaponConfig.ProjectileData projData = null;
        WeaponConfig weaponConfig = configService.getWeaponConfig();
        if (weaponConfig != null && weaponConfig.weapons.containsKey(weapon.type.name())) {
            projData = weaponConfig.weapons.get(weapon.type.name()).projectile;
        }

        for (int i = 0; i < weapon.projectilesPerShot; i++) {
            float finalAngle = baseAngle;
            if (weapon.spread > 0) {
                finalAngle += MathUtils.random(-weapon.spread, weapon.spread);
            }
            spawnProjectile(shooterTransform.x, shooterTransform.y, finalAngle, weapon, shooter.getId(), projData);
        }
    }

    private boolean isBallistic(WeaponComponent.Type type) {
        return type == WeaponComponent.Type.PISTOL || type == WeaponComponent.Type.SHOTGUN || 
               type == WeaponComponent.Type.MACHINE_GUN || type == WeaponComponent.Type.SNIPER_RIFLE;
    }

    private void spawnProjectile(float x, float y, float angle, WeaponComponent weapon, int ownerId, WeaponConfig.ProjectileData config) {
        Entity projectile = entityManager.createEntity();
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        
        float vx = MathUtils.cosDeg(angle) * weapon.projectileSpeed;
        float vy = MathUtils.sinDeg(angle) * weapon.projectileSpeed;
        entityManager.addComponent(projectile, new VelocityComponent(vx, vy));
        
        Color color = Color.YELLOW;
        float radius = 3f;
        float lifetime = 1.5f;
        int damage = 10;
        ProjectileComponent.Behavior behavior = ProjectileComponent.Behavior.NORMAL;
        float explosionRadius = 0f;

        if (config != null) {
            color = parseColor(config.color);
            radius = config.radius;
            lifetime = config.lifetime;
            damage = config.damage;
            behavior = ProjectileComponent.Behavior.valueOf(config.behavior);
            explosionRadius = config.explosionRadius;
        }

        entityManager.addComponent(projectile, new RenderComponent(color, radius, true));
        entityManager.addComponent(projectile, new ProjectileComponent(lifetime, ownerId, damage, behavior, explosionRadius));
        entityManager.addComponent(projectile, new ColliderComponent(radius));
    }

    private Color parseColor(String colorName) {
        try {
            return Color.valueOf(colorName);
        } catch (Exception e) {
            return Color.YELLOW;
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
}
