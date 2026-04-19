package pl.shooter.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import pl.shooter.engine.config.GameConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps physical input (Keyboard, Mouse, GamePad) to abstract GameActions.
 * Handles different input methods (Pressed vs JustPressed).
 */
public class InputMapper {
    private final GameConfig config;
    private final Map<GameAction, Integer> keyMap = new HashMap<>();
    private final Map<GameAction, Integer> mouseButtonMap = new HashMap<>();

    public InputMapper(GameConfig config) {
        this.config = config;
        updateFromConfig();
    }

    public void updateFromConfig() {
        keyMap.clear();
        mouseButtonMap.clear();
        
        GameConfig.InputConfig c = config.controls;
        keyMap.put(GameAction.MOVE_UP, c.moveUpKey);
        keyMap.put(GameAction.MOVE_DOWN, c.moveDownKey);
        keyMap.put(GameAction.MOVE_LEFT, c.moveLeftKey);
        keyMap.put(GameAction.MOVE_RIGHT, c.moveRightKey);
        
        keyMap.put(GameAction.WEAPON_PREV, c.prevWeaponKey);
        keyMap.put(GameAction.WEAPON_NEXT, c.nextWeaponKey);
        
        keyMap.put(GameAction.RELOAD, Input.Keys.R);
        keyMap.put(GameAction.INTERACT, Input.Keys.F);
        keyMap.put(GameAction.PAUSE, Input.Keys.ESCAPE);
        keyMap.put(GameAction.RESTART, Input.Keys.SPACE); // Space also restarts
        
        // Default number keys for weapons
        keyMap.put(GameAction.WEAPON_1, Input.Keys.NUM_1);
        keyMap.put(GameAction.WEAPON_2, Input.Keys.NUM_2);
        keyMap.put(GameAction.WEAPON_3, Input.Keys.NUM_3);
        keyMap.put(GameAction.WEAPON_4, Input.Keys.NUM_4);
        keyMap.put(GameAction.WEAPON_5, Input.Keys.NUM_5);
        keyMap.put(GameAction.WEAPON_6, Input.Keys.NUM_6);
        keyMap.put(GameAction.WEAPON_7, Input.Keys.NUM_7);
        keyMap.put(GameAction.WEAPON_8, Input.Keys.NUM_8);
        keyMap.put(GameAction.WEAPON_9, Input.Keys.NUM_9);
        keyMap.put(GameAction.WEAPON_0, Input.Keys.NUM_0);
        
        // Mouse actions
        mouseButtonMap.put(GameAction.SHOOT, Input.Buttons.LEFT);
    }

    public boolean isPressed(GameAction action) {
        if (keyMap.containsKey(action)) {
            if (Gdx.input.isKeyPressed(keyMap.get(action))) return true;
        }
        if (mouseButtonMap.containsKey(action)) {
            return Gdx.input.isButtonPressed(mouseButtonMap.get(action));
        }
        return false;
    }

    public boolean isJustPressed(GameAction action) {
        if (keyMap.containsKey(action)) {
            if (Gdx.input.isKeyJustPressed(keyMap.get(action))) return true;
        }
        if (mouseButtonMap.containsKey(action)) {
            return Gdx.input.isButtonJustPressed(mouseButtonMap.get(action));
        }
        return false;
    }
}
