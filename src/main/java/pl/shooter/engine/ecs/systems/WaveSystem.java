package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.config.models.GameplayConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.ScoreComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Periodically spawns new enemies around the player and handles waves.
 */
public class WaveSystem extends GameSystem {
    private final EntityFactory entityFactory;
    private final GameMap map;
    private final GameplayConfig config;
    private float spawnTimer = 0;
    private int spawnedCount = 0;

    public WaveSystem(EntityManager entityManager, EntityFactory entityFactory, GameMap map, GameplayConfig config) {
        super(entityManager);
        this.entityFactory = entityFactory;
        this.map = map;
        this.config = config;
    }

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
        if (players.isEmpty()) return;
        
        ScoreComponent score = entityManager.getComponent(players.getFirst(), ScoreComponent.class);

        if (spawnedCount >= 5) {
            score.wave++;
            spawnedCount = 0;
        }

        float spawnInterval = 5.0f;
        if (spawnTimer >= (spawnInterval / (1 + score.wave * 0.1f))) {
            spawnTimer = 0;
            
            int currentEnemies = entityManager.getEntitiesWithComponents(AIComponent.class).size();
            
            int maxEnemiesBase = 8;
            int waveLimit = maxEnemiesBase + score.wave * 2;
            int globalLimit = 100; // Can be moved to EngineConfig later

            if (currentEnemies < Math.min(waveLimit, globalLimit)) {
                spawnEnemyNearPlayer();
                spawnedCount++;
            }
        }
    }

    private void spawnEnemyNearPlayer() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;

        TransformComponent playerTrans = entityManager.getComponent(players.getFirst(), TransformComponent.class);
        
        float spawnX, spawnY;
        int attempts = 0;
        do {
            float angle = MathUtils.random(0, 360);
            float distance = MathUtils.random(400, 800);
            spawnX = playerTrans.x + MathUtils.cosDeg(angle) * distance;
            spawnY = playerTrans.y + MathUtils.sinDeg(angle) * distance;
            attempts++;
        } while (!map.isWalkable(spawnX, spawnY) && attempts < 20);

        if (attempts < 20) {
            // Using new Role/Prefab logic
            String enemyType = MathUtils.randomBoolean() ? "characters/zombie" : "characters/zombie_fat";
            entityFactory.createEnemy(enemyType, spawnX, spawnY);
        }
    }
}
