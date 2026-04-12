package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

public class MenuState extends GameState {

    public MenuState(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void update(float deltaTime) {
        // Switch to play state if mouse is clicked or any key is pressed
        if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
            gsm.setState(new PlayState(gsm));
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.2f, 0.5f, 1); // Blueish background for menu
    }

    @Override
    public void dispose() {}
}
