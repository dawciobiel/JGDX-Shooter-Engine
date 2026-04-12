package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores references to sound effects for different actions.
 */
public class SoundComponent implements Component {
    public enum Action {
        SHOOT,
        HIT,
        DIE,
        WALK,
        EMPTY_CLICK,
        PICKUP // New action for collecting items
    }

    public Map<Action, String> soundPaths = new HashMap<>();

    public SoundComponent() {}

    public void addSound(Action action, String path) {
        soundPaths.put(action, path);
    }
}
