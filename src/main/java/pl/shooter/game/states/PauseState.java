package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

public class PauseState extends GameState {
    private final Stage stage;
    private final Skin skin;

    public PauseState(GameStateManager gsm) {
        super(gsm);
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
        pixmap.setColor(new Color(0, 0, 0, 0.5f)); // Semi-transparent black
        pixmap.fill();
        newSkin.add("background", new Texture(pixmap));

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
        labelStyle.fontColor = Color.WHITE;
        newSkin.add("default", labelStyle);

        return newSkin;
    }

    private void initUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.setBackground(skin.newDrawable("background"));
        stage.addActor(table);

        Label title = new Label("PAUSE", skin);
        title.setFontScale(3f);
        table.add(title).padBottom(50).row();

        TextButton resumeButton = new TextButton("RESUME", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resumeGame();
            }
        });
        table.add(resumeButton).width(200).height(50).padBottom(10).row();

        TextButton menuButton = new TextButton("QUIT TO MENU", skin);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gsm.setState(new MenuState(gsm));
            }
        });
        table.add(menuButton).width(200).height(50);
    }

    private void resumeGame() {
        gsm.pop();
        // Restore input processor to whatever PlayState uses (likely InputSystem)
        // Note: PlayState will handle this in its update/render cycle if needed
    }

    @Override
    public void update(float deltaTime) {
        stage.act(deltaTime);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            resumeGame();
        }
    }

    @Override
    public void render() {
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
