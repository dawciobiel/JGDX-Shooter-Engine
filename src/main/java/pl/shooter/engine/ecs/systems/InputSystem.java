package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cursor;
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
 * Respects the death state of the player entity and uses configurable keys.
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
        this.config = new ConfigService().getConfig();
        
        if (config.ui.useCustomCursor) {
            Gdx.input.setCursorCatched(false);
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        }
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(
                PlayerComponent.class,
                TransformComponent.class,
                VelocityComponent.class
        );

        mouseBuffer.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseBuffer);
        float worldMouseX = mouseBuffer.x;
        float worldMouseY = mouseBuffer.y;

        for (Entity player : players) {
            HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
            if (health != null && health.isDead) continue;

            PlayerComponent pc = entityManager.getComponent(player, PlayerComponent.class);
            VelocityComponent vc = entityManager.getComponent(player, VelocityComponent.class);
            TransformComponent tc = entityManager.getComponent(player, TransformComponent.class);
            InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);

            // 1. Movement
            float vx = 0, vy = 0;
            if (Gdx.input.isKeyPressed(config.controls.moveUpKey)) vy += pc.speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) vy -= pc.speed; // Default S
            if (Gdx.input.isKeyPressed(Input.Keys.A)) vx -= pc.speed; // Default A
            if (Gdx.input.isKeyPressed(Input.Keys.D)) vx += pc.speed; // Default D
            
            vc.vx = vx;
            vc.vy = vy;

            // 2. Aiming
            tc.rotation = MathUtils.atan2(worldMouseY - tc.y, worldMouseX - tc.x) * MathUtils.radiansToDegrees;

            // 3. Shooting (LMB)
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                eventBus.publish(new ShootEvent(player, worldMouseX, worldMouseY));
            }

            // 4. Inventory Selection
            if (inv != null) {
                handleWeaponSelection(inv, player);
            }
        }
    }

    private void handleWeaponSelection(InventoryComponent inv, Entity player) {
        // Direct number selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) inv.currentWeaponIndex = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && inv.weapons.size() > 1) inv.currentWeaponIndex = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && inv.weapons.size() > 2) inv.currentWeaponIndex = 2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && inv.weapons.size() > 3) inv.currentWeaponIndex = 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) && inv.weapons.size() > 4) inv.currentWeaponIndex = 4;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6) && inv.weapons.size() > 5) inv.currentWeaponIndex = 5;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7) && inv.weapons.size() > 6) inv.currentWeaponIndex = 6;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8) && inv.weapons.size() > 7) inv.currentWeaponIndex = 7;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9) && inv.weapons.size() > 8) inv.currentWeaponIndex = 8;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) && inv.weapons.size() > 9) inv.currentWeaponIndex = 9;

        // Cycle selection
        if (Gdx.input.isKeyJustPressed(config.controls.prevWeaponKey)) inv.previousWeapon();
        if (Gdx.input.isKeyJustPressed(config.controls.nextWeaponKey)) inv.nextWeapon();
        
        entityManager.addComponent(player, inv.getActiveWeapon());
    }
}
