package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.world.Tile;

/**
 * Triggered when an entity moves from one type of terrain to another.
 */
public class TerrainChangeEvent implements Event {
    public final Entity entity;
    public final Tile oldTile;
    public final Tile newTile;

    public TerrainChangeEvent(Entity entity, Tile oldTile, Tile newTile) {
        this.entity = entity;
        this.oldTile = oldTile;
        this.newTile = newTile;
    }
}
