package pl.shooter.engine.input;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.config.models.InputConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps physical input to abstract GameActions.
 * Supports merging multiple InputConfigs (Defaults + User Overrides).
 */
public class InputMapper {
    private final Map<GameAction, List<Integer>> keyMap = new HashMap<>();
    private final Map<GameAction, List<Integer>> mouseButtonMap = new HashMap<>();

    public InputMapper(InputConfig defaultConfig, InputConfig userOverrides) {
        mergeConfigs(defaultConfig, userOverrides);
    }

    private void mergeConfigs(InputConfig defaults, InputConfig overrides) {
        keyMap.clear();
        mouseButtonMap.clear();

        // 1. Initial binding from defaults
        applyConfig(defaults);

        // 2. Override with user settings if present
        if (overrides != null) {
            applyConfig(overrides);
        }
    }

    private void applyConfig(InputConfig config) {
        if (config.moveUpKeys != null) bindKeys(GameAction.MOVE_UP, config.moveUpKeys);
        if (config.moveDownKeys != null) bindKeys(GameAction.MOVE_DOWN, config.moveDownKeys);
        if (config.moveLeftKeys != null) bindKeys(GameAction.MOVE_LEFT, config.moveLeftKeys);
        if (config.moveRightKeys != null) bindKeys(GameAction.MOVE_RIGHT, config.moveRightKeys);
        
        if (config.prevWeaponKeys != null) bindKeys(GameAction.WEAPON_PREV, config.prevWeaponKeys);
        if (config.nextWeaponKeys != null) bindKeys(GameAction.WEAPON_NEXT, config.nextWeaponKeys);
        
        if (config.reloadKeys != null) bindKeys(GameAction.RELOAD, config.reloadKeys);
        if (config.interactKeys != null) bindKeys(GameAction.INTERACT, config.interactKeys);
        if (config.pauseKeys != null) bindKeys(GameAction.PAUSE, config.pauseKeys);
        if (config.restartKeys != null) bindKeys(GameAction.RESTART, config.restartKeys);
        
        if (config.weapon1Keys != null) bindKeys(GameAction.WEAPON_1, config.weapon1Keys);
        if (config.weapon2Keys != null) bindKeys(GameAction.WEAPON_2, config.weapon2Keys);
        if (config.weapon3Keys != null) bindKeys(GameAction.WEAPON_3, config.weapon3Keys);
        if (config.weapon4Keys != null) bindKeys(GameAction.WEAPON_4, config.weapon4Keys);
        if (config.weapon5Keys != null) bindKeys(GameAction.WEAPON_5, config.weapon5Keys);
        if (config.weapon6Keys != null) bindKeys(GameAction.WEAPON_6, config.weapon6Keys);
        if (config.weapon7Keys != null) bindKeys(GameAction.WEAPON_7, config.weapon7Keys);
        if (config.weapon8Keys != null) bindKeys(GameAction.WEAPON_8, config.weapon8Keys);
        if (config.weapon9Keys != null) bindKeys(GameAction.WEAPON_9, config.weapon9Keys);
        if (config.weapon0Keys != null) bindKeys(GameAction.WEAPON_0, config.weapon0Keys);
        
        if (config.shootButtons != null) bindButtons(GameAction.SHOOT, config.shootButtons);
    }

    private void bindKeys(GameAction action, int[] keys) {
        // If keys are provided (even if empty []), they overwrite previous setting
        List<Integer> list = keyMap.computeIfAbsent(action, k -> new ArrayList<>());
        list.clear();
        for (int key : keys) list.add(key);
    }

    private void bindButtons(GameAction action, int[] buttons) {
        List<Integer> list = mouseButtonMap.computeIfAbsent(action, k -> new ArrayList<>());
        list.clear();
        for (int button : buttons) list.add(button);
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
