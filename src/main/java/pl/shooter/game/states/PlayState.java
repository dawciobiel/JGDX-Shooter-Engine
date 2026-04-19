package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.config.WeaponConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.input.GameAction;
import pl.shooter.engine.input.InputMapper;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.MapConfig;
import pl.shooter.engine.world.MapService;

import java.util.List;

/**
 * Main gameplay state. 
 * Manages shared rendering resources and receives pre-initialized services.
 */
public class PlayState extends GameState {
    private final Engine engine;
    private final AssetService assetService;
    private final AudioService audioService;
    private final ConfigService configService;
    private final EntityFactory entityFactory;
    private final MapService mapService;
    private final GameConfig config;
    private final String mapPath;
    private final MapConfig mapConfig;
    private final InputMapper inputMapper;
    
    // Shared Rendering Resources
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport uiViewport;
    
    private boolean isGameOver = false;

    public PlayState(GameStateManager gsm, String mapPath, Engine engine, AssetService assetService, 
                     AudioService audioService, ConfigService configService, EntityFactory entityFactory,
                     MapService mapService, MapConfig mapConfig) {
        super(gsm);
        this.mapPath = mapPath;
        this.engine = engine;
        this.assetService = assetService;
        this.audioService = audioService;
        this.configService = configService;
        this.entityFactory = entityFactory;
        this.mapService = mapService;
        this.config = configService.getConfig();
        this.mapConfig = mapConfig;
        this.inputMapper = new InputMapper(configService.getInputConfig());

        // Initialize shared resources
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.uiViewport = new FitViewport(config.graphics.width, config.graphics.height);
        
        initializeSystems();
        Gdx.app.log("PlayState", "Gameplay started for map: " + mapPath);
    }

    private void initializeSystems() {
        if (mapConfig == null) return;

        String mapFolder = mapPath.contains("/") ? mapPath.substring(0, mapPath.lastIndexOf('/')) : config.paths.maps + "/default";
        configService.loadWeaponConfigForMap(mapFolder);
        WeaponConfig weaponConfig = configService.getWeaponConfig();

        GameMap map = mapService.createGameMap(mapConfig);
        
        // Pass shared SpriteBatch and ShapeRenderer to systems
        RenderSystem renderSystem = new RenderSystem(engine.getEntityManager(), assetService, spriteBatch, shapeRenderer);
        renderSystem.setMap(map);
        renderSystem.setShowDebugPaths(config.debug.showPaths);
        renderSystem.setShowDebugHitboxes(config.debug.showHitboxes);

        LightSystem lightSystem = new LightSystem(engine.getEntityManager(), spriteBatch);
        lightSystem.setAmbientColor(mapConfig.settings.ambientColor.r, mapConfig.settings.ambientColor.g, mapConfig.settings.ambientColor.b, mapConfig.settings.ambientColor.a);
        renderSystem.setLightSystem(lightSystem);

        UISystem uiSystem = new UISystem(engine.getEntityManager(), assetService, spriteBatch, shapeRenderer);
        uiSystem.setShowFps(config.debug.showFps);
        uiSystem.init(engine);

        AISystem aiSystem = new AISystem(engine.getEntityManager(), engine.getEventBus());
        aiSystem.setMap(map);

        engine.addSystem(new InputSystem(engine.getEntityManager(), engine.getEventBus(), renderSystem.getCamera()));
        engine.addSystem(new PathfindingSystem(engine.getEntityManager(), map, engine.getEventBus())); 
        engine.addSystem(aiSystem);
        engine.addSystem(new SteeringSystem(engine.getEntityManager())); 
        engine.addSystem(new CombatSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory, configService, map, config));
        engine.addSystem(new ProjectileSystem(engine.getEntityManager()));
        engine.addSystem(new ParticleUpdateSystem(engine.getEntityManager()));
        engine.addSystem(new PushingSystem(engine.getEntityManager(), map));
        engine.addSystem(new InteractionSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new MapSystem(engine.getEntityManager(), map, engine.getEventBus()));
        engine.addSystem(new MovementSystem(engine.getEntityManager(), map)); 
        engine.addSystem(new TriggerSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new CollisionSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new DamageSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory, config));
        engine.addSystem(new SoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService, assetService));
        engine.addSystem(new AmbientSoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService, assetService));
        engine.addSystem(new MultiKillSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new AnimationSystem(engine.getEntityManager()));
        engine.addSystem(new WaveSystem(engine.getEntityManager(), entityFactory, map, config));
        engine.addSystem(renderSystem);
        engine.addSystem(uiSystem);

        mapService.spawnEntities(mapConfig);
        
        setupPlayer(weaponConfig);
        checkGameOver();
    }

    private void setupPlayer(WeaponConfig weaponConfig) {
        List<Entity> players = engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class);
        if (!players.isEmpty()) {
            Entity player = players.getFirst();
            engine.getEntityManager().addComponent(player, new LightComponent(400f, new Color(1, 1, 0.9f, 1f), 0.9f));
            InventoryComponent inv = engine.getEntityManager().getComponent(player, InventoryComponent.class);
            if (inv == null) inv = new InventoryComponent();
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.KNIFE, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.PISTOL, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.SHOTGUN, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.MACHINE_GUN, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.SNIPER_RIFLE, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.PLASMA_GUN, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.ROCKET_LAUNCHER, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.LIGHTNING_GUN, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.RAIL_GUN, weaponConfig));
            inv.addWeapon(WeaponComponent.create(WeaponComponent.Type.GRENADE, weaponConfig));
            inv.currentWeaponIndex = 1;
            engine.getEntityManager().addComponent(player, inv);
            engine.getEntityManager().addComponent(player, inv.getActiveWeapon());
        }
    }

    private void checkGameOver() {
        List<Entity> players = engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class);
        if (players.isEmpty()) {
            isGameOver = true;
            return;
        }
        HealthComponent health = engine.getEntityManager().getComponent(players.getFirst(), HealthComponent.class);
        if (health != null && health.isDead) isGameOver = true;
    }

    @Override
    public void update(float deltaTime) {
        if (inputMapper.isJustPressed(GameAction.PAUSE) && !isGameOver) {
            gsm.push(new PauseState(gsm));
            return;
        }

        if (isGameOver) {
            updateGameOverSystems(deltaTime);
            if (inputMapper.isJustPressed(GameAction.RESTART)) {
                gsm.setAbsoluteState(new LoadingState(gsm, mapPath));
            }
            return;
        }

        engine.update(deltaTime);
        checkGameOver();
    }

    private void updateGameOverSystems(float deltaTime) {
        RenderSystem rs = null;
        UISystem ui = null;
        for (pl.shooter.engine.ecs.GameSystem system : engine.getSystems()) {
            if (system instanceof RenderSystem) rs = (RenderSystem) system;
            if (system instanceof UISystem) ui = (UISystem) system;
        }
        if (rs != null) rs.update(deltaTime);
        if (ui != null) ui.update(deltaTime);
    }

    @Override public void render() {
    }

    @Override 
    public void resize(int width, int height) {
        if (uiViewport != null) uiViewport.update(width, height, true);
        if (engine != null) engine.resize(width, height);
    }
    
    @Override 
    public void dispose() {
        Gdx.app.log("PlayState", "Disposing PlayState and shared resources");
        if (engine != null) engine.dispose();
        if (assetService != null) assetService.dispose();
        if (audioService != null) audioService.dispose();
        
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();

        if (config != null && config.ui.useCustomCursor) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }
}
