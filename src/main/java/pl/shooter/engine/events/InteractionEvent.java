package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.components.InteractableComponent;

/**
 * Triggered when a player interacts with an object (e.g., pressing 'E').
 */
public class InteractionEvent implements Event {
    public final Entity player;
    public final Entity interactable;
    public final InteractableComponent.Type type;
    public final String targetId;

    public InteractionEvent(Entity player, Entity interactable, InteractableComponent component) {
        this.player = player;
        this.interactable = interactable;
        this.type = component.type;
        this.targetId = component.targetId;
    }
}
