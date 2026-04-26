package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.*;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.components.HealthComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.input.InputMapper;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.MapConfig;
import pl.shooter.engine.world.MapService;

import java.util.List;

/**
 * Main gameplay state.
 */
public class PlayState extends GameState {
    private final Engine engine;
    private final AssetService assetService;
    private final AudioService audioService;
    private final ConfigService configService;
    private final EntityFactory entityFactory;
    private final MapService mapService;
    private final MapConfig mapConfig;
    private final EngineConfig engineConfig;
    private final RenderingConfig renderingConfig;
    private final GameplayConfig gameplayConfig;
    private final InputMapper inputMapper;
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport uiViewport;
    private final String mapPath;
    private boolean isGameOver = false;

    public PlayState(GameStateManager gsm, String mapPath, Engine engine, AssetService assetService, 
                     AudioService audioService, ConfigService configService, EntityFactory entityFactory,
                     MapService mapService, MapConfig mapConfig,
                     EngineConfig engineConfig, RenderingConfig renderingConfig, GameplayConfig gameplayConfig,
                     InputConfig defaultInput, InputConfig userOverrides) {
        super(gsm);
        this.mapPath = mapPath;
        this.engine = engine;
        this.assetService = assetService;
        this.audioService = audioService;
        this.configService = configService;
        this.entityFactory = entityFactory;
        this.mapService = mapService;
        this.mapConfig = mapConfig;
        this.engineConfig = engineConfig;
        this.renderingConfig = renderingConfig;
        this.gameplayConfig = gameplayConfig;
        this.inputMapper = new InputMapper(defaultInput, userOverrides);
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.uiViewport = new FitViewport(renderingConfig.width, renderingConfig.height);
        initializeSystems();
    }

    private void initializeSystems() {
        GameMap map = mapService.createGameMap(mapConfig);
        RenderSystem renderSystem = new RenderSystem(engine.getEntityManager(), assetService, spriteBatch, shapeRenderer);
        renderSystem.init(engineConfig, gameplayConfig);
        renderSystem.setMap(map);
        renderSystem.setShowDebugPaths(engineConfig.debug.showPaths);
        renderSystem.setShowDebugHitboxes(engineConfig.debug.showHitboxes);

        LightSystem lightSystem = new LightSystem(engine.getEntityManager(), spriteBatch);
        lightSystem.setAmbientColor(renderingConfig.ambientColor.r, renderingConfig.ambientColor.g, renderingConfig.ambientColor.b, renderingConfig.ambientColor.a);
        renderSystem.setLightSystem(lightSystem);

        UISystem uiSystem = new UISystem(engine.getEntityManager(), assetService, spriteBatch, shapeRenderer);
        uiSystem.setRenderingConfig(renderingConfig); // Fixed: Pass config for cursor
        uiSystem.init(engine, engineConfig);

        AISystem aiSystem = new AISystem(engine.getEntityManager(), engine.getEventBus());
        aiSystem.setMap(map);

        engine.addSystem(new InputSystem(engine.getEntityManager(), engine.getEventBus(), renderSystem.getCamera(), inputMapper));
        engine.addSystem(new PathfindingSystem(engine.getEntityManager(), map, engine.getEventBus())); 
        engine.addSystem(aiSystem);
        engine.addSystem(new SteeringSystem(engine.getEntityManager())); 
        engine.addSystem(new CombatSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory, configService, map, renderingConfig));
        engine.addSystem(new ProjectileSystem(engine.getEntityManager()));
        engine.addSystem(new ParticleUpdateSystem(engine.getEntityManager()));
        engine.addSystem(new PushingSystem(engine.getEntityManager(), map));
        engine.addSystem(new InteractionSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new MapSystem(engine.getEntityManager(), map, engine.getEventBus()));
        engine.addSystem(new MovementSystem(engine.getEntityManager(), map)); 
        engine.addSystem(new TriggerSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new CollisionSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new DamageSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory, gameplayConfig));
        engine.addSystem(new SoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService, assetService));
        engine.addSystem(new AmbientSoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService, assetService));
        engine.addSystem(new MultiKillSystem(engine.getEntityManager(), engine.getEventBus(), gameplayConfig));
        engine.addSystem(new AnimationSystem(engine.getEntityManager()));
        engine.addSystem(new WaveSystem(engine.getEntityManager(), entityFactory, map, gameplayConfig));
        engine.addSystem(renderSystem);
        engine.addSystem(uiSystem);

        mapService.spawnEntities(mapConfig);
        checkGameOver();
    }

    private void checkGameOver() {
        List<Entity> players = engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class);
        if (players.isEmpty()) { isGameOver = true; return; }
        HealthComponent health = engine.getEntityManager().getComponent(players.getFirst(), HealthComponent.class);
        if (health != null && health.isDead) isGameOver = true;
    }

    @Override
    public void update(float deltaTime) {
        if (inputMapper.isJustPressed(pl.shooter.engine.input.GameAction.PAUSE) && !isGameOver) {
            gsm.push(new PauseState(gsm));
            return;
        }
        if (isGameOver) {
            if (inputMapper.isJustPressed(pl.shooter.engine.input.GameAction.RESTART)) {
                gsm.setAbsoluteState(new LoadingState(gsm, mapPath));
            }
            return;
        }
        engine.update(deltaTime);
        checkGameOver();
    }

    @Override public void render() {}
    @Override public void resize(int width, int height) {
        if (uiViewport != null) uiViewport.update(width, height, true);
        if (engine != null) engine.resize(width, height);
    }
    @Override public void dispose() {
        if (engine != null) engine.dispose();
        if (assetService != null) assetService.dispose();
        if (audioService != null) audioService.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (renderingConfig != null && renderingConfig.ui.useCustomCursor) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }
}
