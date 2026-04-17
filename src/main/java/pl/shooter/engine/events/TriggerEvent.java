package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.components.TriggerComponent;

/**
 * Fired when an entity enters or exits a trigger zone.
 */
public class TriggerEvent implements Event {
    public enum State { ENTER, EXIT }

    public final Entity entity;       // The entity that triggered it (e.g. Player)
    public final Entity triggerEntity; // The entity holding the TriggerComponent
    public final State state;
    public final TriggerComponent.TriggerType type;
    public final String value;

    public TriggerEvent(Entity entity, Entity triggerEntity, State state, TriggerComponent trigger) {
        this.entity = entity;
        this.triggerEntity = triggerEntity;
        this.state = state;
        this.type = trigger.type;
        this.value = trigger.value;
    }
}
