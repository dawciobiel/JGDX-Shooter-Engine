package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.ecs.components.VelocityComponent;
import pl.shooter.engine.ecs.components.WeaponComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.ShootEvent;

import java.util.List;

/**
 * Controls AI-driven entities.
 * NPCs can chase the player and fire weapons if within range.
 */
public class AISystem extends GameSystem {
    private final EventBus eventBus;

    public AISystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        // 1. Find the player (assume one player for simplicity)
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;
        
        Entity player = players.get(0);
        TransformComponent playerTrans = entityManager.getComponent(player, TransformComponent.class);

        // 2. Process AI entities
        List<Entity> enemies = entityManager.getEntitiesWithComponents(
                AIComponent.class, TransformComponent.class, VelocityComponent.class
        );

        for (Entity enemy : enemies) {
            AIComponent ai = entityManager.getComponent(enemy, AIComponent.class);
            TransformComponent enemyTrans = entityManager.getComponent(enemy, TransformComponent.class);
            VelocityComponent enemyVel = entityManager.getComponent(enemy, VelocityComponent.class);

            float distance = Vector2.dst(enemyTrans.x, enemyTrans.y, playerTrans.x, playerTrans.y);

            if (distance < ai.detectRange) {
                // Direction to player
                Vector2 direction = new Vector2(playerTrans.x - enemyTrans.x, playerTrans.y - enemyTrans.y);
                direction.nor();

                // Rotate towards player
                enemyTrans.rotation = direction.angleDeg();

                // Logic based on behavior
                if (ai.behavior == AIComponent.Behavior.CHASE) {
                    // Constant speed for enemies (placeholder 100)
                    enemyVel.vx = direction.x * 100f;
                    enemyVel.vy = direction.y * 100f;
                }

                // If enemy has a weapon, try to shoot the player
                if (entityManager.hasComponent(enemy, WeaponComponent.class)) {
                    eventBus.publish(new ShootEvent(enemy, playerTrans.x, playerTrans.y));
                }
            } else {
                // Stop if player is out of range
                enemyVel.vx = 0;
                enemyVel.vy = 0;
            }
        }
    }
}
