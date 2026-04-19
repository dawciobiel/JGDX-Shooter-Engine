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
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;

import java.util.ArrayList;
import java.util.List;

public class MenuState extends GameState {
    private final Stage stage;
    private final Skin skin;
    private final GameConfig config;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        this.config = new ConfigService().getConfig();
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        this.stage = new Stage(new FitViewport(config.graphics.width, config.graphics.height));
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

        // Button Style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = newSkin.newDrawable("white", Color.DARK_GRAY);
        buttonStyle.down = newSkin.newDrawable("white", Color.GRAY);
        buttonStyle.over = newSkin.newDrawable("white", Color.LIGHT_GRAY);
        buttonStyle.font = newSkin.getFont("default");
        newSkin.add("default", buttonStyle);

        // Label Style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = newSkin.getFont("default");
        labelStyle.fontColor = Color.YELLOW;
        newSkin.add("default", labelStyle);

        // ScrollPane Style
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

        // Map list table
        Table mapTable = new Table();
        List<String> maps = discoverMaps();
        
        for (String mapPath : maps) {
            String mapName = mapPath.substring(mapPath.lastIndexOf("/") + 1);
            TextButton mapBtn = new TextButton(mapName.toUpperCase().replace("_", " "), skin);
            mapBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Directly switch to PlayState, it handles its own loading screen
                    String fullMapPath = mapPath + "/" + config.paths.mapFileName;
                    gsm.setAbsoluteState(new PlayState(gsm, fullMapPath));
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
        FileHandle mapsDir = Gdx.files.internal(config.paths.maps);
        if (mapsDir.exists() && mapsDir.isDirectory()) {
            for (FileHandle folder : mapsDir.list()) {
                if (folder.isDirectory() && folder.child(config.paths.mapFileName).exists()) {
                    mapFolders.add(folder.path());
                }
            }
        }
        if (mapFolders.isEmpty()) {
            mapFolders.add(config.paths.maps + "/testing_room");
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
