package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.AmmoPrefab;
import pl.shooter.engine.config.models.RenderingConfig;
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

/**
 * Handles the logic of combat, weapon firing, reloading, and projectile spawning.
 * Updated to use new Prefab-based architecture and Ammo Categories.
 */
public class CombatSystem extends GameSystem {
    private final EventBus eventBus;
    private final EntityFactory entityFactory;
    private final ConfigService configService;
    private final GameMap map;
    private final RenderingConfig renderingConfig;
    private float totalTime = 0;

    public CombatSystem(EntityManager entityManager, EventBus eventBus, EntityFactory entityFactory, ConfigService configService, GameMap map, RenderingConfig renderingConfig) {
        super(entityManager);
        this.eventBus = eventBus;
        this.entityFactory = entityFactory;
        this.configService = configService;
        this.map = map;
        this.renderingConfig = renderingConfig;
        eventBus.subscribe(ShootEvent.class, this::handleShoot);
    }

    @Override
    public void update(float deltaTime) {
        totalTime += deltaTime;
        for (Entity entity : entityManager.getEntitiesWithComponents(InventoryComponent.class)) {
            InventoryComponent inv = entityManager.getComponent(entity, InventoryComponent.class);
            for (WeaponComponent weapon : inv.weapons) {
                updateWeaponState(weapon, inv, deltaTime);
            }
        }
    }

    private void updateWeaponState(WeaponComponent weapon, InventoryComponent inv, float deltaTime) {
        if (weapon.isReloading) {
            weapon.reloadTimer += deltaTime;
            if (weapon.reloadTimer >= weapon.reloadTime) {
                finishReload(weapon, inv);
            }
        } else if (weapon.magazineAmmo <= 0 && weapon.magazineSize > 0) {
            // Auto-reload if empty
            startReload(weapon, inv);
        }
    }

    private void handleShoot(ShootEvent event) {
        Entity shooter = event.shooter;
        WeaponComponent weapon = entityManager.getComponent(shooter, WeaponComponent.class);
        TransformComponent shooterTransform = entityManager.getComponent(shooter, TransformComponent.class);

        if (weapon == null || shooterTransform == null || weapon.isReloading) return;
        if (totalTime - weapon.lastShotTime < weapon.fireRate) return;

        // Check ammo
        if (weapon.magazineAmmo <= 0 && weapon.magazineSize > 0) {
            eventBus.publish(new EmptyWeaponEvent(shooter));
            weapon.lastShotTime = totalTime; 
            return;
        }

        // --- Execute Shot ---
        weapon.lastShotTime = totalTime;
        if (weapon.magazineSize > 0) {
            weapon.magazineAmmo--;
        }

        // Audio and Shells
        eventBus.publish(new BulletFiredEvent(shooter, weapon.shootSound));
        
        float baseAngle = MathUtils.atan2(event.targetY - shooterTransform.y, event.targetX - shooterTransform.x) * MathUtils.radiansToDegrees;
        if (entityFactory != null) {
             entityFactory.createShellEjection(shooterTransform.x, shooterTransform.y, baseAngle);
        }

        // Spawn Projectiles based on current AmmoPrefab
        if (weapon.activeAmmo != null) {
            AmmoPrefab ammo = weapon.activeAmmo;
            for (int i = 0; i < ammo.projectilesPerUnit; i++) {
                float finalAngle = baseAngle;
                float currentSpread = weapon.spread * ammo.spreadMultiplier;
                if (currentSpread > 0) {
                    finalAngle += MathUtils.random(-currentSpread, currentSpread);
                }
                entityFactory.createProjectile(ammo.projectilePrefabPath, shooter, shooterTransform.x, shooterTransform.y, finalAngle);
            }
        } else {
            Gdx.app.error("CombatSystem", "Weapon fired without activeAmmo: " + weapon.name);
        }
    }

    private void startReload(WeaponComponent weapon, InventoryComponent inv) {
        if (weapon.isReloading || weapon.activeAmmo == null) return;
        
        int available = inv.getAmmoCount(weapon.activeAmmo.id);
        if (available > 0) {
            weapon.isReloading = true;
            weapon.reloadTimer = 0;
        }
    }

    private void finishReload(WeaponComponent weapon, InventoryComponent inv) {
        if (!weapon.isReloading || weapon.activeAmmo == null) return;
        
        int needed = weapon.magazineSize - weapon.magazineAmmo;
        int available = inv.getAmmoCount(weapon.activeAmmo.id);
        
        int toTransfer = Math.min(needed, available);
        inv.ammoCounts.put(weapon.activeAmmo.id, available - toTransfer);
        weapon.magazineAmmo += toTransfer;
        
        weapon.isReloading = false;
    }
}
