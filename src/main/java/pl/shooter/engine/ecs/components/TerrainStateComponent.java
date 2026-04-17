package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.world.Tile;

/**
 * Stores the current terrain type an entity is standing on.
 */
public class TerrainStateComponent implements Component {
    public Tile currentTile = Tile.GROUND;
}
