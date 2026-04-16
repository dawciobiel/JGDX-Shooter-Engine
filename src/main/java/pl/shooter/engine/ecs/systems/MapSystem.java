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
 * Handles collisions between entities and the game map (walls), applies terrain speed modifiers,
 * and triggers TerrainChangeEvents.
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
            ColliderComponent collider = entityManager.getComponent(entity, ColliderComponent.class);
            
            float radius = (collider != null) ? collider.radius : 0f;

            // 1. Terrain Detection & Speed Modification
            Tile tileAtCenter = currentMap.getTile(transform.x, transform.y);
            float terrainMultiplier = (tileAtCenter != null) ? tileAtCenter.speedMultiplier : 1.0f;

            // 2. Terrain Change Event logic (for players or tagged entities)
            if (entityManager.hasComponent(entity, PlayerComponent.class)) {
                handleTerrainChange(entity, tileAtCenter);
            }
            
            // 3. Projectile handling
            if (entityManager.hasComponent(entity, ProjectileComponent.class)) {
                if (!isAreaWalkable(transform.x, transform.y, radius)) {
                    entityManager.removeEntity(entity);
                }
                continue;
            }

            // 4. Character Sliding Logic & Velocity Apply
            // Apply terrain multiplier to the step
            float stepX = (velocity.vx * terrainMultiplier) * deltaTime;
            float nextX = transform.x + stepX;
            if (isAreaWalkable(nextX, transform.y, radius)) {
                transform.x = nextX;
            }

            float stepY = (velocity.vy * terrainMultiplier) * deltaTime;
            float nextY = transform.y + stepY;
            if (isAreaWalkable(transform.x, nextY, radius)) {
                transform.y = nextY;
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

    private boolean isAreaWalkable(float x, float y, float radius) {
        float offset = radius * 0.707f;
        return currentMap.isWalkable(x, y) &&
               currentMap.isWalkable(x + radius, y) &&
               currentMap.isWalkable(x - radius, y) &&
               currentMap.isWalkable(x, y + radius) &&
               currentMap.isWalkable(x, y - radius) &&
               currentMap.isWalkable(x + offset, y + offset) &&
               currentMap.isWalkable(x - offset, y + offset) &&
               currentMap.isWalkable(x + offset, y - offset) &&
               currentMap.isWalkable(x - offset, y - offset);
    }
}
