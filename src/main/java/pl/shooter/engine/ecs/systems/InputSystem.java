package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

import java.util.List;

public class InputSystem extends GameSystem {
    private final EventBus eventBus;
    private final OrthographicCamera camera;
    private final Vector3 mouseBuffer = new Vector3();

    public InputSystem(EntityManager entityManager, EventBus eventBus, OrthographicCamera camera) {
        super(entityManager);
        this.eventBus = eventBus;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(
                PlayerComponent.class,
                TransformComponent.class,
                VelocityComponent.class
        );

        // Convert mouse screen pos to world pos using the camera
        mouseBuffer.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseBuffer);
        float worldMouseX = mouseBuffer.x;
        float worldMouseY = mouseBuffer.y;

        for (Entity player : players) {
            PlayerComponent pc = entityManager.getComponent(player, PlayerComponent.class);
            VelocityComponent vc = entityManager.getComponent(player, VelocityComponent.class);
            TransformComponent tc = entityManager.getComponent(player, TransformComponent.class);

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
        }
    }
}
