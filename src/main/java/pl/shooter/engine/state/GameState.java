package pl.shooter.engine.state;

/**
 * Base class for different game states (Menu, Play, Pause, etc.)
 */
public abstract class GameState {
    protected final GameStateManager gsm;

    protected GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void update(float deltaTime);
    public abstract void render();
    public abstract void dispose();
    
    /**
     * Called when the window is resized.
     */
    public void resize(int width, int height) {}
}
