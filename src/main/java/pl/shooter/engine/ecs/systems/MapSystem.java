package pl.shooter.engine.ecs.systems;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.TerrainChangeEvent;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.Tile;

import java.util.List;

/**
 * Handles terrain detection and applies terrain speed modifiers to VelocityComponent.
 */
public class MapSystem extends GameSystem {
    private final GameMap currentMap;
    private final EventBus eventBus;

    public MapSystem(EntityManager entityManager, GameMap map, EventBus eventBus) {
        super(entityManager);
        this.currentMap = map;
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        if (currentMap == null) return;

        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, VelocityComponent.class);

        for (Entity entity : entities) {
            TransformComponent transform = entityManager.getComponent(entity, TransformComponent.class);
            VelocityComponent velocity = entityManager.getComponent(entity, VelocityComponent.class);

            // 1. Terrain Detection
            Tile tileAtCenter = currentMap.getTile(transform.x, transform.y);
            velocity.terrainMultiplier = (tileAtCenter != null) ? tileAtCenter.speedMultiplier : 1.0f;

            // 2. Terrain Change Event (for players)
            if (entityManager.hasComponent(entity, PlayerComponent.class)) {
                handleTerrainChange(entity, tileAtCenter);
            }
        }
    }

    private void handleTerrainChange(Entity entity, Tile currentTile) {
        TerrainStateComponent terrainState = entityManager.getComponent(entity, TerrainStateComponent.class);
        if (terrainState == null) {
            terrainState = new TerrainStateComponent();
            terrainState.currentTile = currentTile;
            entityManager.addComponent(entity, terrainState);
            return;
        }
        if (terrainState.currentTile != currentTile) {
            Tile oldTile = terrainState.currentTile;
            terrainState.currentTile = currentTile;
            eventBus.publish(new TerrainChangeEvent(entity, oldTile, currentTile));
        }
    }
}
