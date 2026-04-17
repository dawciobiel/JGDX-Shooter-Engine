package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Marks an entity as interactable by the player (e.g., switches, doors, messages).
 */
public class InteractableComponent implements Component {
    public enum Type {
        SWITCH,
        DOOR,
        PICKUP,
        NPC,
        MESSAGE
    }

    public Type type = Type.SWITCH;
    public String targetId; // ID of the entity this interaction affects OR the message text
    public float interactionRadius = 40f;
    public boolean onceOnly = false;
    public boolean activated = false;

    public InteractableComponent() {}
    public InteractableComponent(Type type, String targetId) {
        this.type = type;
        this.targetId = targetId;
    }
}
