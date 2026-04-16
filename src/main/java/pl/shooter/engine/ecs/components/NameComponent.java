package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores the name/title of an entity to be displayed in the UI/HUD.
 */
public class NameComponent implements Component {
    public String name;

    public NameComponent() {}
    public NameComponent(String name) {
        this.name = name;
    }
}
