package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;

import java.util.List;

/**
 * Periodically spawns new enemies around the player.
 */
public class WaveSystem extends GameSystem {
    private final EntityFactory entityFactory;
    private final float spawnInterval = 5.0f; // Spawn every 5 seconds
    private float spawnTimer = 0;
    private final int maxEnemies = 20;

    public WaveSystem(EntityManager entityManager, EntityFactory entityFactory) {
        super(entityManager);
        this.entityFactory = entityFactory;
    }

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;

        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            
            // Check current enemy count to prevent overcrowding
            int currentEnemies = entityManager.getEntitiesWithComponents(AIComponent.class).size();
            if (currentEnemies < maxEnemies) {
                spawnEnemyNearPlayer();
            }
        }
    }

    private void spawnEnemyNearPlayer() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;

        TransformComponent playerTrans = entityManager.getComponent(players.get(0), TransformComponent.class);

        // Pick a random angle and distance (outside the screen, roughly 500-700 units away)
        float angle = MathUtils.random(0, 360);
        float distance = MathUtils.random(500, 700);
        
        float spawnX = playerTrans.x + MathUtils.cosDeg(angle) * distance;
        float spawnY = playerTrans.y + MathUtils.sinDeg(angle) * distance;

        // Spawn a zombie at the calculated position
        entityFactory.loadFromJson("assets/entities/zombie.json", spawnX, spawnY);
    }
}
