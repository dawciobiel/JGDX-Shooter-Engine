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
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.RenderingConfig;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

/**
 * Pause Menu Screen.
 */
public class PauseState extends GameState {
    private final Stage stage;
    private final Skin skin;

    public PauseState(GameStateManager gsm) {
        super(gsm);
        ConfigService configService = new ConfigService();
        RenderingConfig renderingConfig = configService.getRenderingConfig();

        this.stage = new Stage(new FitViewport(renderingConfig.width, renderingConfig.height));
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
        newSkin.add("background", new Texture(pixmap));
        newSkin.add("white", new Texture(pixmap));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = newSkin.newDrawable("white", Color.DARK_GRAY);
        buttonStyle.down = newSkin.newDrawable("white", Color.GRAY);
        buttonStyle.over = newSkin.newDrawable("white", Color.LIGHT_GRAY);
        buttonStyle.font = newSkin.getFont("default");
        newSkin.add("default", buttonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = newSkin.getFont("default");
        newSkin.add("default", labelStyle);

        return newSkin;
    }

    private void initUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.setBackground(skin.newDrawable("background", new Color(0, 0, 0, 0.7f)));
        stage.addActor(table);

        Label title = new Label("PAUSE", skin);
        title.setFontScale(2.0f);
        table.add(title).padBottom(30).row();

        TextButton resumeButton = new TextButton("RESUME", skin);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gsm.pop();
            }
        });
        table.add(resumeButton).width(200).height(50).padBottom(10).row();

        TextButton menuButton = new TextButton("QUIT TO MENU", skin);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gsm.setAbsoluteState(new MenuState(gsm));
            }
        });
        table.add(menuButton).width(200).height(50);
    }

    @Override
    public void update(float deltaTime) {
        stage.act(deltaTime);
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
