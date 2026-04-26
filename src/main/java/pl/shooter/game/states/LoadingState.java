package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.*;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.MapConfig;
import pl.shooter.engine.world.MapService;

import java.util.List;

/**
 * Professional Loading State with Asset Error Reporting.
 */
public class LoadingState extends GameState {
    private final String mapPath;
    private final Stage stage;
    private final Skin skin;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Table errorTable;
    
    private final ConfigService configService;
    private final EngineConfig engineConfig;
    private final RenderingConfig renderingConfig;
    private final GameplayConfig gameplayConfig;
    private final InputConfig defaultInputConfig;
    private final InputConfig userInputConfig;

    private AssetService assetService;
    private AudioService audioService;
    private MapService mapService;
    private MapConfig mapConfig;
    private Engine engine;
    private EntityFactory entityFactory;
    
    private boolean loadStarted = false;
    private float minLoadTimer = 0;
    private static final float MIN_LOAD_TIME = 0.8f;

    public LoadingState(GameStateManager gsm, String mapPath) {
        super(gsm);
        this.mapPath = mapPath;
        this.configService = new ConfigService();
        this.engineConfig = configService.getEngineConfig();
        this.renderingConfig = configService.getRenderingConfig();
        this.gameplayConfig = configService.getGameplayConfig();
        this.defaultInputConfig = configService.getDefaultInputConfig();
        this.userInputConfig = configService.getUserInputConfig();

        this.stage = new Stage(new FitViewport(renderingConfig.width, renderingConfig.height));
        this.skin = createLoadingSkin();
        initUI();
    }

    private Skin createLoadingSkin() {
        Skin newSkin = new Skin();
        BitmapFont font = new BitmapFont();
        newSkin.add("default", font);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        newSkin.add("white", new Texture(pixmap));

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = newSkin.newDrawable("white", Color.DARK_GRAY);
        barStyle.knobBefore = newSkin.newDrawable("white", Color.GOLD);
        newSkin.add("default-horizontal", barStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        newSkin.add("default", labelStyle);
        
        Label.LabelStyle errorStyle = new Label.LabelStyle();
        errorStyle.font = font;
        errorStyle.fontColor = Color.RED;
        newSkin.add("error", errorStyle);

        return newSkin;
    }

    private void initUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("LOADING...", skin);
        title.setFontScale(1.5f);
        root.add(title).padBottom(20).row();

        progressBar = new ProgressBar(0, 1, 0.01f, false, skin);
        root.add(progressBar).width(400).height(20).padBottom(10).row();

        statusLabel = new Label("Preparing engine...", skin);
        statusLabel.setFontScale(0.8f);
        root.add(statusLabel).padBottom(20).row();
        
        errorTable = new Table();
        root.add(errorTable).growX();
    }

    private void startLoading() {
        this.engine = new Engine();
        this.assetService = new AssetService(engineConfig);
        this.audioService = new AudioService();
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService, configService);
        this.mapService = new MapService(entityFactory, assetService, configService);
        
        assetService.setCurrentMapFolder(mapPath);
        this.mapConfig = mapService.loadMap(mapPath);
        
        // Use RenderingConfig for UI cursor - simplified call
        if (renderingConfig.ui.useCustomCursor && renderingConfig.ui.cursorImagePath != null) {
            assetService.loadTexture(renderingConfig.ui.cursorImagePath);
        }
        
        loadStarted = true;
    }

    @Override
    public void update(float deltaTime) {
        if (!loadStarted) {
            startLoading();
            return;
        }

        minLoadTimer += deltaTime;
        boolean assetsLoaded = assetService.update();
        progressBar.setValue(assetService.getProgress());

        List<String> errors = assetService.getLoadingErrors();
        if (!errors.isEmpty()) {
            updateErrorDisplay(errors);
            statusLabel.setText("MISSING ASSETS DETECTED!");
            statusLabel.setColor(Color.RED);
        }

        // If no errors and loading finished, proceed
        if (assetsLoaded && minLoadTimer >= MIN_LOAD_TIME && errors.isEmpty()) {
            gsm.setAbsoluteState(new PlayState(gsm, mapPath, engine, assetService, audioService, configService, entityFactory, mapService, mapConfig,
                    engineConfig, renderingConfig, gameplayConfig, defaultInputConfig, userInputConfig));
        }
        
        stage.act(deltaTime);
    }

    private void updateErrorDisplay(List<String> errors) {
        errorTable.clear();
        Label errorTitle = new Label("ERROR: Failed to load some files:", skin, "error");
        errorTable.add(errorTitle).padBottom(5).row();
        for (String error : errors) {
            Label errLabel = new Label(error, skin, "error");
            errLabel.setFontScale(0.7f);
            errorTable.add(errLabel).row();
        }
    }

    @Override public void render() {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
