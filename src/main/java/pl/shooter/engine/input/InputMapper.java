package pl.shooter.engine.input;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.config.InputConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps physical input to abstract GameActions.
 * Supports multiple bindings per action.
 */
public class InputMapper {
    private final InputConfig config;
    private final Map<GameAction, List<Integer>> keyMap = new HashMap<>();
    private final Map<GameAction, List<Integer>> mouseButtonMap = new HashMap<>();

    public InputMapper(InputConfig config) {
        this.config = config;
        updateFromConfig();
    }

    public void updateFromConfig() {
        keyMap.clear();
        mouseButtonMap.clear();
        
        bindKeys(GameAction.MOVE_UP, config.moveUpKeys);
        bindKeys(GameAction.MOVE_DOWN, config.moveDownKeys);
        bindKeys(GameAction.MOVE_LEFT, config.moveLeftKeys);
        bindKeys(GameAction.MOVE_RIGHT, config.moveRightKeys);
        
        bindKeys(GameAction.WEAPON_PREV, config.prevWeaponKeys);
        bindKeys(GameAction.WEAPON_NEXT, config.nextWeaponKeys);
        
        bindKeys(GameAction.RELOAD, config.reloadKeys);
        bindKeys(GameAction.INTERACT, config.interactKeys);
        bindKeys(GameAction.PAUSE, config.pauseKeys);
        bindKeys(GameAction.RESTART, config.restartKeys);
        
        bindKeys(GameAction.WEAPON_1, config.weapon1Keys);
        bindKeys(GameAction.WEAPON_2, config.weapon2Keys);
        bindKeys(GameAction.WEAPON_3, config.weapon3Keys);
        bindKeys(GameAction.WEAPON_4, config.weapon4Keys);
        bindKeys(GameAction.WEAPON_5, config.weapon5Keys);
        bindKeys(GameAction.WEAPON_6, config.weapon6Keys);
        bindKeys(GameAction.WEAPON_7, config.weapon7Keys);
        bindKeys(GameAction.WEAPON_8, config.weapon8Keys);
        bindKeys(GameAction.WEAPON_9, config.weapon9Keys);
        bindKeys(GameAction.WEAPON_0, config.weapon0Keys);
        
        bindButtons(GameAction.SHOOT, config.shootButtons);
    }

    private void bindKeys(GameAction action, int[] keys) {
        if (keys == null) return;
        keyMap.computeIfAbsent(action, k -> new ArrayList<>()).clear();
        for (int key : keys) keyMap.get(action).add(key);
    }

    private void bindButtons(GameAction action, int[] buttons) {
        if (buttons == null) return;
        mouseButtonMap.computeIfAbsent(action, k -> new ArrayList<>()).clear();
        for (int button : buttons) mouseButtonMap.get(action).add(button);
    }

    public boolean isPressed(GameAction action) {
        if (keyMap.containsKey(action)) {
            for (int key : keyMap.get(action)) {
                if (Gdx.input.isKeyPressed(key)) return true;
            }
        }
        if (mouseButtonMap.containsKey(action)) {
            for (int button : mouseButtonMap.get(action)) {
                if (Gdx.input.isButtonPressed(button)) return true;
            }
        }
        return false;
    }

    public boolean isJustPressed(GameAction action) {
        if (keyMap.containsKey(action)) {
            for (int key : keyMap.get(action)) {
                if (Gdx.input.isKeyJustPressed(key)) return true;
            }
        }
        if (mouseButtonMap.containsKey(action)) {
            for (int button : mouseButtonMap.get(action)) {
                if (Gdx.input.isButtonJustPressed(button)) return true;
            }
        }
        return false;
    }
}
