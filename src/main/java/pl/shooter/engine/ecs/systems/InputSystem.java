package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

import java.util.List;

/**
 * Handles player input for movement, aiming, and weapon management.
 * Now respects the death state of the player entity.
 */
public class InputSystem extends GameSystem {
    private final EventBus eventBus;
    private final OrthographicCamera camera;
    private final Vector3 mouseBuffer = new Vector3();
    private final GameConfig config;

    public InputSystem(EntityManager entityManager, EventBus eventBus, OrthographicCamera camera) {
        super(entityManager);
        this.eventBus = eventBus;
        this.camera = camera;
        this.config = new ConfigService().getConfig(); // Load config for controls
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(
                PlayerComponent.class,
                TransformComponent.class,
                VelocityComponent.class,
                InventoryComponent.class
        );

        mouseBuffer.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseBuffer);
        float worldMouseX = mouseBuffer.x;
        float worldMouseY = mouseBuffer.y;

        for (Entity player : players) {
            HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
            if (health != null && health.isDead) {
                // Ignore input for dead player
                continue;
            }

            PlayerComponent pc = entityManager.getComponent(player, PlayerComponent.class);
            VelocityComponent vc = entityManager.getComponent(player, VelocityComponent.class);
            TransformComponent tc = entityManager.getComponent(player, TransformComponent.class);
            InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);

            // 1. Movement
            float vx = 0;
            float vy = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) vy += pc.speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) vy -= pc.speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) vx -= pc.speed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) vx += pc.speed;
            vc.vx = vx;
            vc.vy = vy;

            // 2. Aiming
            tc.rotation = MathUtils.atan2(worldMouseY - tc.y, worldMouseX - tc.x) * MathUtils.radiansToDegrees;

            // 3. Shooting
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                eventBus.publish(new ShootEvent(player, worldMouseX, worldMouseY));
            }

            // 4. Inventory / Weapon Selection
            if (inv != null) {
                // Number keys for direct access
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inv.currentWeaponIndex = 0 % inv.weapons.size();
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && inv.weapons.size() > 1) inv.currentWeaponIndex = 1;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && inv.weapons.size() > 2) inv.currentWeaponIndex = 2;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && inv.weapons.size() > 3) inv.currentWeaponIndex = 3;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) && inv.weapons.size() > 4) inv.currentWeaponIndex = 4;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6) && inv.weapons.size() > 5) inv.currentWeaponIndex = 5;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7) && inv.weapons.size() > 6) inv.currentWeaponIndex = 6;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8) && inv.weapons.size() > 7) inv.currentWeaponIndex = 7;
                if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9) && inv.weapons.size() > 8) inv.currentWeaponIndex = 8;

                // Cycle keys from config
                if (Gdx.input.isKeyJustPressed(config.controls.prevWeaponKey)) inv.previousWeapon();
                if (Gdx.input.isKeyJustPressed(config.controls.nextWeaponKey)) inv.nextWeapon();
                
                // Update the active WeaponComponent on the entity so other systems see it
                entityManager.addComponent(player, inv.getActiveWeapon());
            }
        }
    }
}
