package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

/**
 * Handles combat logic, including weapon types and firing patterns.
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
    }

    private void handleShoot(ShootEvent event) {
        Entity shooter = event.shooter;
        WeaponComponent weapon = entityManager.getComponent(shooter, WeaponComponent.class);
        TransformComponent shooterTransform = entityManager.getComponent(shooter, TransformComponent.class);

        if (weapon == null || shooterTransform == null) return;

        // Cooldown check
        if (totalTime - weapon.lastShotTime < weapon.fireRate) {
            return;
        }

        weapon.lastShotTime = totalTime;

        // Base angle to target
        float baseAngle = MathUtils.atan2(event.targetY - shooterTransform.y, event.targetX - shooterTransform.x) * MathUtils.radiansToDegrees;

        // Fire multiple projectiles if weapon allows (e.g. Shotgun)
        for (int i = 0; i < weapon.projectilesPerShot; i++) {
            float finalAngle = baseAngle;
            
            // Add random spread
            if (weapon.spread > 0) {
                finalAngle += MathUtils.random(-weapon.spread, weapon.spread);
            }

            spawnProjectile(shooterTransform.x, shooterTransform.y, finalAngle, weapon.projectileSpeed, shooter.getId());
        }
    }

    private void spawnProjectile(float x, float y, float angle, float speed, int ownerId) {
        Entity projectile = entityManager.createEntity();
        
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        
        // Calculate velocity vector from angle
        float vx = MathUtils.cosDeg(angle) * speed;
        float vy = MathUtils.sinDeg(angle) * speed;
        entityManager.addComponent(projectile, new VelocityComponent(vx, vy));
        
        entityManager.addComponent(projectile, new RenderComponent(Color.YELLOW, 3f, true));
        entityManager.addComponent(projectile, new ProjectileComponent(1.5f, ownerId));
        entityManager.addComponent(projectile, new ColliderComponent(3f));
    }
}
