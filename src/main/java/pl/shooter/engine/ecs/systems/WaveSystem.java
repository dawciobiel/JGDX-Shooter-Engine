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
import pl.shooter.engine.world.GameMap;

import java.util.List;

/**
 * Periodically spawns new enemies around the player and handles waves.
 */
public class WaveSystem extends GameSystem {
    private final EntityFactory entityFactory;
    private final GameMap map;
    private final float spawnInterval = 5.0f;
    private float spawnTimer = 0;
    private final int maxEnemiesBase = 8;
    private int spawnedCount = 0;

    public WaveSystem(EntityManager entityManager, EntityFactory entityFactory, GameMap map) {
        super(entityManager);
        this.entityFactory = entityFactory;
        this.map = map;
    }

    @Override
    public void update(float deltaTime) {
        spawnTimer += deltaTime;

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, ScoreComponent.class);
        if (players.isEmpty()) return;
        
        ScoreComponent score = entityManager.getComponent(players.get(0), ScoreComponent.class);

        // Wave increase logic
        if (spawnedCount >= 5) {
            score.wave++;
            spawnedCount = 0;
        }

        if (spawnTimer >= (spawnInterval / (1 + score.wave * 0.1f))) {
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
        
        float spawnX, spawnY;
        int attempts = 0;
        do {
            float angle = MathUtils.random(0, 360);
            float distance = MathUtils.random(400, 800);
            spawnX = playerTrans.x + MathUtils.cosDeg(angle) * distance;
            spawnY = playerTrans.y + MathUtils.sinDeg(angle) * distance;
            attempts++;
        } while (!map.isWalkable(spawnX, spawnY) && attempts < 10);

        if (attempts < 10) {
            entityFactory.loadFromJson("assets/entities/zombie.json", spawnX, spawnY);
        }
    }
}
