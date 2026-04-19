package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
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
import pl.shooter.engine.input.GameAction;
import pl.shooter.engine.input.InputMapper;

import java.util.List;

/**
 * Handles player input for movement, aiming, and weapon management.
 * Uses abstract GameActions via InputMapper for decoupling physical input.
 */
public class InputSystem extends GameSystem {
    private final EventBus eventBus;
    private final OrthographicCamera camera;
    private final Vector3 mouseBuffer = new Vector3();
    private final InputMapper inputMapper;

    public InputSystem(EntityManager entityManager, EventBus eventBus, OrthographicCamera camera) {
        super(entityManager);
        this.eventBus = eventBus;
        this.camera = camera;
        GameConfig config = new ConfigService().getConfig();
        this.inputMapper = new InputMapper(config);
        
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

            // 1. Movement using abstract actions
            float vx = 0, vy = 0;
            if (inputMapper.isPressed(GameAction.MOVE_UP)) vy += pc.speed;
            if (inputMapper.isPressed(GameAction.MOVE_DOWN)) vy -= pc.speed;
            if (inputMapper.isPressed(GameAction.MOVE_LEFT)) vx -= pc.speed;
            if (inputMapper.isPressed(GameAction.MOVE_RIGHT)) vx += pc.speed;
            
            vc.vx = vx;
            vc.vy = vy;

            // 2. Aiming
            tc.rotation = MathUtils.atan2(worldMouseY - tc.y, worldMouseX - tc.x) * MathUtils.radiansToDegrees;

            // 3. Shooting using abstract actions
            if (inputMapper.isPressed(GameAction.SHOOT)) {
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
        if (inputMapper.isJustPressed(GameAction.WEAPON_1)) inv.currentWeaponIndex = 0;
        if (inputMapper.isJustPressed(GameAction.WEAPON_2) && inv.weapons.size() > 1) inv.currentWeaponIndex = 1;
        if (inputMapper.isJustPressed(GameAction.WEAPON_3) && inv.weapons.size() > 2) inv.currentWeaponIndex = 2;
        if (inputMapper.isJustPressed(GameAction.WEAPON_4) && inv.weapons.size() > 3) inv.currentWeaponIndex = 3;
        if (inputMapper.isJustPressed(GameAction.WEAPON_5) && inv.weapons.size() > 4) inv.currentWeaponIndex = 4;
        if (inputMapper.isJustPressed(GameAction.WEAPON_6) && inv.weapons.size() > 5) inv.currentWeaponIndex = 5;
        if (inputMapper.isJustPressed(GameAction.WEAPON_7) && inv.weapons.size() > 6) inv.currentWeaponIndex = 6;
        if (inputMapper.isJustPressed(GameAction.WEAPON_8) && inv.weapons.size() > 7) inv.currentWeaponIndex = 7;
        if (inputMapper.isJustPressed(GameAction.WEAPON_9) && inv.weapons.size() > 8) inv.currentWeaponIndex = 8;
        if (inputMapper.isJustPressed(GameAction.WEAPON_0) && inv.weapons.size() > 9) inv.currentWeaponIndex = 9;

        // Cycle selection
        if (inputMapper.isJustPressed(GameAction.WEAPON_PREV)) inv.previousWeapon();
        if (inputMapper.isJustPressed(GameAction.WEAPON_NEXT)) inv.nextWeapon();
        
        entityManager.addComponent(player, inv.getActiveWeapon());
    }
}
