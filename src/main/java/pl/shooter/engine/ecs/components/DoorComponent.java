package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Stores the state and properties of a door.
 */
public class DoorComponent implements Component {
    public boolean isOpen = false;
    public String doorId; // Link this to InteractableComponent.targetId
    public float openSpeed = 2.0f;
    
    // For visual change
    public String closedTexture;
    public String openTexture;

    public DoorComponent() {}
    public DoorComponent(String doorId) {
        this.doorId = doorId;
    }
}
