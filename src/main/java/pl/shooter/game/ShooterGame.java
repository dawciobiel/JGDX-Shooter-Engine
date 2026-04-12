package pl.shooter.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.game.states.MenuState;

/**
 * Entry point that hands over control to the GameStateManager.
 */
public class ShooterGame extends ApplicationAdapter {
    private GameStateManager gsm;

    @Override
    public void create() {
        gsm = new GameStateManager();
        gsm.push(new MenuState(gsm));
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        gsm.update(deltaTime);
        gsm.render();
    }

    @Override
    public void dispose() {
        gsm.dispose();
    }
}
