package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.ScoreComponent;
import pl.shooter.engine.ecs.components.TransformComponent;

import java.util.List;

/**
 * Periodically spawns new enemies around the player and handles waves.
 */
public class WaveSystem extends GameSystem {
    private final EntityFactory entityFactory;
    private final float spawnInterval = 5.0f;
    private float spawnTimer = 0;
    private final int maxEnemiesBase = 5;
    private int spawnedCount = 0;

    public WaveSystem(EntityManager entityManager, EntityFactory entityFactory) {
        super(entityManager);
        this.entityFactory = entityFactory;
    }

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
        if (players.isEmpty()) return;
        
        ScoreComponent score = entityManager.getComponent(players.get(0), ScoreComponent.class);

        // Wave increase logic (every 5 enemies spawned)
        if (spawnedCount >= 5) {
            score.wave++;
            spawnedCount = 0;
        }

        if (spawnTimer >= (spawnInterval / (1 + score.wave * 0.1f))) { // Spawn faster as waves go
            spawnTimer = 0;
            
            int currentEnemies = entityManager.getEntitiesWithComponents(AIComponent.class).size();
            if (currentEnemies < (maxEnemiesBase + score.wave * 2)) {
                spawnEnemyNearPlayer();
                spawnedCount++;
            }
        }
    }

    private void spawnEnemyNearPlayer() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;

        TransformComponent playerTrans = entityManager.getComponent(players.get(0), TransformComponent.class);
        float angle = MathUtils.random(0, 360);
        float distance = MathUtils.random(500, 700);
        float spawnX = playerTrans.x + MathUtils.cosDeg(angle) * distance;
        float spawnY = playerTrans.y + MathUtils.sinDeg(angle) * distance;

        entityFactory.loadFromJson("assets/entities/zombie.json", spawnX, spawnY);
    }
}
