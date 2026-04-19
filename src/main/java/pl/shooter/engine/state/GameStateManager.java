package pl.shooter.engine.state;

import com.badlogic.gdx.Gdx;
import java.util.Stack;

/**
 * Manages a stack of game states. 
 * Allows pushing, popping, and switching between states.
 */
public class GameStateManager {
    private final Stack<GameState> states;

    public GameStateManager() {
        this.states = new Stack<>();
    }

    /**
     * Pushes a new state and ensures it is resized to current window dimensions.
     */
    public void push(GameState state) {
        states.push(state);
        // CRITICAL: Ensure the new state knows about the current screen size immediately
        state.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void pop() {
        if (!states.isEmpty()) {
            states.pop().dispose();
        }
    }

    /**
     * Replaces the top state with a new one.
     */
    public void setState(GameState state) {
        pop();
        push(state);
    }

    /**
     * Clears the entire stack and sets the new state as the only one.
     * Essential for returning to main menu or starting a fresh game.
     */
    public void setAbsoluteState(GameState state) {
        while (!states.isEmpty()) {
            states.pop().dispose();
        }
        push(state);
    }

    public void update(float deltaTime) {
        if (!states.isEmpty()) {
            states.peek().update(deltaTime);
        }
    }

    public void render() {
        for (GameState state : states) {
            state.render();
        }
    }

    public void resize(int width, int height) {
        for (GameState state : states) {
            state.resize(width, height);
        }
    }

    public void dispose() {
        while (!states.isEmpty()) {
            states.pop().dispose();
        }
    }
}
