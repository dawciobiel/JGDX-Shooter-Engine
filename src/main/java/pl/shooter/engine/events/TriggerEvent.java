package pl.shooter.engine.events;

import pl.shooter.engine.ecs.Entity;

/**
 * Event fired when a trigger is activated.
 */
public class TriggerEvent implements Event {
    public enum Type {
        MESSAGE,
        TELEPORT,
        AMBIENT_SOUND,
        STOP_AMBIENT,
        MUSIC_CHANGE,
        STOP_MUSIC,
        TRAP,
        GAME_OVER,
        LEVEL_COMPLETE,
        SPAWN_WAVE
    }

    public enum State {
        ENTER,
        EXIT
    }

    public final Entity activator;
    public final Type type;
    public final String value;
    public final State state;
    public final boolean isLooping;
    public final float volume;

    public TriggerEvent(Entity activator, Type type, String value, State state) {
        this(activator, type, value, state, true, 0.7f);
    }

    public TriggerEvent(Entity activator, Type type, String value, State state, boolean isLooping, float volume) {
        this.activator = activator;
        this.type = type;
        this.value = value;
        this.state = state;
        this.isLooping = isLooping;
        this.volume = volume;
    }
}
