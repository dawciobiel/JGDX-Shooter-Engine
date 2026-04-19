package pl.shooter.game.states;

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
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.MapConfig;
import pl.shooter.engine.world.MapService;

/**
 * Professional Loading State using Scene2D for UI.
 * Handles asynchronous asset loading and initialization before switching to PlayState.
 */
public class LoadingState extends GameState {
    private final String mapPath;
    
    // UI
    private final Stage stage;
    private final Skin skin;
    private ProgressBar progressBar;
    private Label statusLabel;
    
    // Logic/Services
    private final ConfigService configService;
    private final GameConfig config;
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
        this.config = configService.getConfig();
        
        this.stage = new Stage(new FitViewport(config.graphics.width, config.graphics.height));
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
        Texture whiteTexture = new Texture(pixmap);
        newSkin.add("white", whiteTexture);

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = newSkin.newDrawable("white", Color.DARK_GRAY);
        barStyle.knobBefore = newSkin.newDrawable("white", Color.GOLD);
        newSkin.add("default-horizontal", barStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        newSkin.add("default", labelStyle);

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
        root.add(statusLabel);
    }

    private void startLoading() {
        this.engine = new Engine();
        this.assetService = new AssetService(config);
        this.audioService = new AudioService();
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService, config);
        this.mapService = new MapService(entityFactory, assetService);
        
        // Setup folder context BEFORE loading map
        String mapFolder = mapPath.contains("/") ? mapPath.substring(0, mapPath.lastIndexOf('/')) : config.paths.maps + "/default";
        assetService.setCurrentMapFolder(mapFolder);
        entityFactory.setCurrentMapFolder(mapFolder);
        
        // Queue map assets and entity templates
        this.mapConfig = mapService.loadMap(mapPath);
        
        if (config.ui.useCustomCursor && config.ui.cursorImagePath != null) {
            assetService.loadTexture(config.ui.cursorImagePath);
        }
        
        audioService.loadSound(assetService.resolvePath("characters/soldier/hit.wav", config.paths.sounds));
        audioService.loadSound(assetService.resolvePath("characters/soldier/death.wav", config.paths.sounds));
        
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
        float progress = assetService.getProgress();
        
        progressBar.setValue(progress);
        
        if (progress < 0.5f) {
            statusLabel.setText("Loading map data...");
        } else if (progress < 0.9f) {
            statusLabel.setText("Loading textures and sounds...");
        } else {
            statusLabel.setText("Finalizing initialization...");
        }

        if (assetsLoaded && minLoadTimer >= MIN_LOAD_TIME) {
            goToPlayState();
        }
        
        stage.act(deltaTime);
    }

    private void goToPlayState() {
        PlayState playState = new PlayState(gsm, mapPath, engine, assetService, audioService, configService, entityFactory, mapService, mapConfig);
        gsm.setAbsoluteState(playState);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1);
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
