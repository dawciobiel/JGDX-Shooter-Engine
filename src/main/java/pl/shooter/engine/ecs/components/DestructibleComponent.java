package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as a destructible environment object.
 */
public class DestructibleComponent implements Component {
    public boolean blocksMovement = true;

    public DestructibleComponent() {}
    public DestructibleComponent(boolean blocksMovement) {
        this.blocksMovement = blocksMovement;
    }
}
