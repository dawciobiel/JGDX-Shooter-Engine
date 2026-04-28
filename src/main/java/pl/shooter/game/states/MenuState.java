package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.EngineConfig;
import pl.shooter.engine.config.models.RenderingConfig;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Menu Screen.
 * Updated to use new ConfigService and Directory Structure.
 */
public class MenuState extends GameState {
    private final Stage stage;
    private final Skin skin;
    private final ConfigService configService;
    private final EngineConfig engineConfig;
    private final RenderingConfig renderingConfig;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        this.configService = new ConfigService();
        this.engineConfig = configService.getEngineConfig();
        this.renderingConfig = configService.getRenderingConfig();

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
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

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = newSkin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.5f));
        newSkin.add("default", scrollStyle);

        return newSkin;
    }

    private void initUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("SHOOTER ENGINE", skin);
        title.setFontScale(2.5f);
        root.add(title).padBottom(30).row();

        Label selectMapLabel = new Label("SELECT MAP:", skin);
        selectMapLabel.setFontScale(1.2f);
        root.add(selectMapLabel).padBottom(10).row();

        Table mapTable = new Table();
        List<String> maps = discoverMaps();
        
        for (String mapPath : maps) {
            String mapName = mapPath.substring(mapPath.lastIndexOf("/") + 1);
            TextButton mapBtn = new TextButton(mapName.toUpperCase().replace("_", " "), skin);
            mapBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // mapPath is already full internal path: assets/maps/name
                    gsm.setAbsoluteState(new LoadingState(gsm, mapPath));
                }
            });
            mapTable.add(mapBtn).width(300).height(40).padBottom(5).row();
        }

        ScrollPane scroll = new ScrollPane(mapTable, skin);
        scroll.setFadeScrollBars(false);
        root.add(scroll).width(350).height(250).padBottom(20).row();

        TextButton exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        root.add(exitButton).width(200).height(50);
    }

    private List<String> discoverMaps() {
        List<String> mapFolders = new ArrayList<>();
        FileHandle mapsDir = Gdx.files.internal(engineConfig.paths.mapsRoot);
        if (mapsDir.exists() && mapsDir.isDirectory()) {
            for (FileHandle folder : mapsDir.list()) {
                // Check if it's a map folder by looking for map.json
                if (folder.isDirectory() && folder.child("map.json").exists()) {
                    mapFolders.add(folder.path());
                }
            }
        }
        return mapFolders;
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
