package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
import pl.shooter.engine.world.GameMap;
import pl.shooter.events.HitEvent;

import java.util.List;

public class CombatSystem extends GameSystem {
    private final EventBus eventBus;
    private final EntityFactory entityFactory;
    private final ConfigService configService;
    private final GameMap map;
    private float totalTime = 0;

    public CombatSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory, ConfigService configService, GameMap map) {
        super(entityManager);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        this.configService = configService;
        this.map = map;
        eventBus.subscribe(ShootEvent.class, this::handleShoot);
    }

    @Override
    public void update(float deltaTime) {
        totalTime += deltaTime;
        for (Entity entity : entityManager.getEntitiesWithComponents(InventoryComponent.class)) {
            InventoryComponent inv = entityManager.getComponent(entity, InventoryComponent.class);
            for (WeaponComponent weapon : inv.weapons) {
                if (weapon.isReloading) {
                    weapon.reloadTimer += deltaTime;
                    if (weapon.reloadTimer >= weapon.reloadTime) finishReload(weapon);
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

        if (weapon == null || shooterTransform == null || weapon.isReloading) return;
        if (totalTime - weapon.lastShotTime < weapon.fireRate) return;

        if (weapon.type == WeaponComponent.Type.KNIFE) {
            handleMeleeAttack(shooter, shooterTransform, weapon, event.targetX, event.targetY);
            weapon.lastShotTime = totalTime;
            return;
        }

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
        if (weapon.magazineSize > 0) weapon.magazineAmmo--;

        // PUBLISH EVENT WITH WEAPON SOUND
        eventBus.publish(new BulletFiredEvent(shooter, weapon.shootSound));

        float baseAngle = MathUtils.atan2(event.targetY - shooterTransform.y, event.targetX - shooterTransform.x) * MathUtils.radiansToDegrees;
        if (entityFactory != null && isBallistic(weapon.type)) {
            entityFactory.createShellEjection(shooterTransform.x, shooterTransform.y, baseAngle);
        }

        WeaponConfig.ProjectileData projData = null;
        WeaponConfig weaponConfig = configService.getWeaponConfig();
        if (weaponConfig != null && weaponConfig.weapons.containsKey(weapon.type.name())) {
            projData = weaponConfig.weapons.get(weapon.type.name()).projectile;
        }

        for (int i = 0; i < weapon.projectilesPerShot; i++) {
            float finalAngle = baseAngle;
            if (weapon.spread > 0) finalAngle += MathUtils.random(-weapon.spread, weapon.spread);
            spawnProjectile(shooterTransform.x, shooterTransform.y, finalAngle, weapon, shooter.getId(), projData);
        }
    }

    private void handleMeleeAttack(Entity shooter, TransformComponent t, WeaponComponent weapon, float targetX, float targetY) {
        eventBus.publish(new BulletFiredEvent(shooter, weapon.shootSound)); 

        float angle = MathUtils.atan2(targetY - t.y, targetX - t.x) * MathUtils.radiansToDegrees;
        float cos = MathUtils.cosDeg(angle), sin = MathUtils.sinDeg(angle);
        
        List<Entity> targets = entityManager.getEntitiesWithComponents(TransformComponent.class, HealthComponent.class, ColliderComponent.class);
        for (Entity victim : targets) {
            if (victim.getId() == shooter.getId()) continue;
            TransformComponent vt = entityManager.getComponent(victim, TransformComponent.class);
            ColliderComponent vc = entityManager.getComponent(victim, ColliderComponent.class);
            float dx = vt.x - t.x, dy = vt.y - t.y;
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            
            if (dist <= weapon.range + vc.radius) {
                if (!isLineOfSightClear(t.x, t.y, vt.x, vt.y)) continue;
                float dot = (dx/dist) * cos + (dy/dist) * sin;
                if (dot > 0.7f) eventBus.publish(new HitEvent(victim, shooter.getId(), weapon.damage)); 
            }
        }
    }

    private boolean isLineOfSightClear(float x1, float y1, float x2, float y2) {
        if (map == null) return true;
        float dist = Vector2.dst(x1, y1, x2, y2);
        int steps = (int) (dist / 8); 
        float dx = (x2 - x1) / steps, dy = (y2 - y1) / steps;
        for (int i = 1; i < steps; i++) {
            if (!map.isWalkable(x1 + dx * i, y1 + dy * i)) return false;
        }
        return true;
    }

    private boolean isBallistic(WeaponComponent.Type type) {
        return type == WeaponComponent.Type.PISTOL || type == WeaponComponent.Type.SHOTGUN || 
               type == WeaponComponent.Type.MACHINE_GUN || type == WeaponComponent.Type.SNIPER_RIFLE;
    }

    private void spawnProjectile(float x, float y, float angle, WeaponComponent weapon, int ownerId, WeaponConfig.ProjectileData config) {
        Entity projectile = entityManager.createEntity();
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        entityManager.addComponent(projectile, new VelocityComponent(MathUtils.cosDeg(angle) * weapon.projectileSpeed, MathUtils.sinDeg(angle) * weapon.projectileSpeed));
        
        Color color = Color.YELLOW;
        float radius = 3f, lifetime = 1.5f;
        int damage = 10;
        ProjectileComponent.Behavior behavior = ProjectileComponent.Behavior.NORMAL;
        float explosionRadius = 0f;

        if (config != null) {
            try { color = Color.valueOf(config.color); } catch (Exception e) {}
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
