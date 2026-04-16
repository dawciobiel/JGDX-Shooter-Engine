package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

/**
 * Defines an area that triggers specific actions when entered or exited.
 */
public class TriggerComponent implements Component {
    public enum TriggerType {
        AMBIENT_SOUND,
        MUSIC_CHANGE,
        TRAP,
        MESSAGE,
        ZONE_ENTER
    }

    public TriggerType type;
    public String value;       // e.g., "tunnel_dark_ambient.ogg" or "trap_01"
    public boolean oneShot = false;
    public boolean activated = false;
    public boolean isPlayerInside = false;

    public TriggerComponent(TriggerType type, String value) {
        this.type = type;
        this.value = value;
    }
}
