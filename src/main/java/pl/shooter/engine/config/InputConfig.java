package pl.shooter.engine.config;

import com.badlogic.gdx.Input;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuration for all input mappings.
 * Supports multiple keys/buttons per action.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputConfig {
    public int[] moveUpKeys = {Input.Keys.W, Input.Keys.UP};
    public int[] moveDownKeys = {Input.Keys.S, Input.Keys.DOWN};
    public int[] moveLeftKeys = {Input.Keys.A, Input.Keys.LEFT};
    public int[] moveRightKeys = {Input.Keys.D, Input.Keys.RIGHT};
    
    public int[] prevWeaponKeys = {Input.Keys.Q};
    public int[] nextWeaponKeys = {Input.Keys.E};
    public int[] reloadKeys = {Input.Keys.R};
    public int[] interactKeys = {Input.Keys.F};
    
    public int[] pauseKeys = {Input.Keys.ESCAPE};
    public int[] restartKeys = {Input.Keys.SPACE, Input.Keys.R};
    
    public int[] weapon1Keys = {Input.Keys.NUM_1};
    public int[] weapon2Keys = {Input.Keys.NUM_2};
    public int[] weapon3Keys = {Input.Keys.NUM_3};
    public int[] weapon4Keys = {Input.Keys.NUM_4};
    public int[] weapon5Keys = {Input.Keys.NUM_5};
    public int[] weapon6Keys = {Input.Keys.NUM_6};
    public int[] weapon7Keys = {Input.Keys.NUM_7};
    public int[] weapon8Keys = {Input.Keys.NUM_8};
    public int[] weapon9Keys = {Input.Keys.NUM_9};
    public int[] weapon0Keys = {Input.Keys.NUM_0};
    
    public int[] shootButtons = {Input.Buttons.LEFT};
}
