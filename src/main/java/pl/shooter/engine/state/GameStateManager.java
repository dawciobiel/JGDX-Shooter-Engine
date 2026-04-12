package pl.shooter.engine.state;

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

    public void push(GameState state) {
        states.push(state);
    }

    public void pop() {
        if (!states.isEmpty()) {
            states.pop().dispose();
        }
    }

    public void setState(GameState state) {
        if (!states.isEmpty()) {
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
        if (!states.isEmpty()) {
            states.peek().render();
        }
    }

    public void resize(int width, int height) {
        if (!states.isEmpty()) {
            states.peek().resize(width, height);
        }
    }

    public void dispose() {
        while (!states.isEmpty()) {
            states.pop().dispose();
        }
    }
}
