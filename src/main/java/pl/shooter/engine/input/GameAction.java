package pl.shooter.engine.input;

/**
 * All possible player actions in the game.
 * Decouples physical input (keys/buttons) from gameplay logic.
 */
public enum GameAction {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    
    SHOOT,
    RELOAD,
    INTERACT,
    
    WEAPON_NEXT,
    WEAPON_PREV,
    WEAPON_1,
    WEAPON_2,
    WEAPON_3,
    WEAPON_4,
    WEAPON_5,
    WEAPON_6,
    WEAPON_7,
    WEAPON_8,
    WEAPON_9,
    WEAPON_0,
    
    PAUSE,
    RESTART
}
