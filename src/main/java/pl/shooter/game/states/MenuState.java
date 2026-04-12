package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

public class MenuState extends GameState {
    private final Stage stage;
    private final Skin skin;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        // Use FitViewport to maintain 800x600 ratio regardless of screen size
        this.stage = new Stage(new FitViewport(800, 600));
        this.skin = createBasicSkin();
        Gdx.input.setInputProcessor(stage);

        initUI();
    }

    private Skin createBasicSkin() {
        Skin newSkin = new Skin();
        BitmapFont font = new BitmapFont();
        newSkin.add("default", font);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        newSkin.add("white", new Texture(pixmap));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = newSkin.newDrawable("white", Color.DARK_GRAY);
        buttonStyle.down = newSkin.newDrawable("white", Color.GRAY);
        buttonStyle.over = newSkin.newDrawable("white", Color.LIGHT_GRAY);
        buttonStyle.font = newSkin.getFont("default");
        newSkin.add("default", buttonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = newSkin.getFont("default");
        labelStyle.fontColor = Color.YELLOW;
        newSkin.add("default", labelStyle);

        return newSkin;
    }

    private void initUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("SHOOTER ENGINE", skin);
        title.setFontScale(2.5f);
        table.add(title).padBottom(50).row();

        TextButton playButton = new TextButton("START GAME", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gsm.setState(new PlayState(gsm));
            }
        });
        table.add(playButton).width(200).height(50).padBottom(10).row();

        TextButton exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).width(200).height(50);
    }

    @Override
    public void update(float deltaTime) {
        stage.act(deltaTime);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
