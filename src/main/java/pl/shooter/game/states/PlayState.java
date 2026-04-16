package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.config.WeaponConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.MapConfig;
import pl.shooter.engine.world.MapService;

import java.util.List;

public class PlayState extends GameState {
    private Engine engine;
    private AssetService assetService;
    private AudioService audioService;
    private final ConfigService configService;
    private EntityFactory entityFactory;
    private MapService mapService;
    private boolean isGameOver = false;
    private final GameConfig config;
    private final String mapPath;

    public PlayState(GameStateManager gsm, String mapPath) {
        super(gsm);
        this.mapPath = mapPath;
        this.configService = new ConfigService();
        this.config = configService.getConfig();
        resetState();
    }

    private void resetState() {
        if (engine != null) dispose();

        this.engine = new Engine();
        this.assetService = new AssetService();
        this.audioService = new AudioService();
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService);
        this.mapService = new MapService(engine.getEntityManager(), entityFactory, assetService);
        this.isGameOver = false;

        init();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void init() {
        MapConfig mapConfig = mapService.loadMap(mapPath);
        if (mapConfig == null) {
            Gdx.app.error("PlayState", "CRITICAL: Could not load map: " + mapPath);
            return;
        }

        assetService.loadTexture("assets/graphics/textures/characters/soldier/walk.png");
        for (int i = 0; i <= 16; i++) {
            assetService.loadTexture("assets/graphics/textures/characters/zombies/skeleton/skeleton-idle_" + i + ".png");
            assetService.loadTexture("assets/graphics/textures/characters/zombies/skeleton/skeleton-move_" + i + ".png");
        }
        for (int i = 0; i <= 8; i++) assetService.loadTexture("assets/graphics/textures/characters/zombies/skeleton/skeleton-attack_" + i + ".png");
        
        assetService.loadTexture("assets/graphics/textures/characters/combat-robot/combat-robot-walk.png");
        assetService.loadTexture("assets/graphics/textures/characters/combat-robot/combat-robot-shoot.png");
        assetService.loadTexture("assets/graphics/textures/characters/combat-robot/combat-robot-explode.png");

        if (config.ui.useCustomCursor && config.ui.cursorImagePath != null && !config.ui.cursorImagePath.isEmpty()) {
            assetService.loadTexture(config.ui.cursorImagePath);
        }

        assetService.finishLoading();

        audioService.loadSound("assets/audio/sfx/characters/soldier/hit.wav");
        audioService.loadSound("assets/audio/sfx/characters/soldier/death.wav");

        WeaponConfig weaponConfig = configService.getWeaponConfig();
        GameMap map = mapService.createGameMap(mapConfig);
        
        RenderSystem renderSystem = new RenderSystem(engine.getEntityManager(), assetService);
        renderSystem.setMap(map);
        renderSystem.setShowDebugPaths(config.debug.showPaths);
        renderSystem.setShowDebugHitboxes(config.debug.showHitboxes);

        LightSystem lightSystem = new LightSystem(engine.getEntityManager());
        lightSystem.setAmbientColor(
            mapConfig.settings.ambientColor.r, 
            mapConfig.settings.ambientColor.g, 
            mapConfig.settings.ambientColor.b, 
            mapConfig.settings.ambientColor.a
        );
        renderSystem.setLightSystem(lightSystem);

        UISystem uiSystem = new UISystem(engine.getEntityManager(), assetService);
        uiSystem.setShowFps(config.debug.showFps);

        AISystem aiSystem = new AISystem(engine.getEntityManager(), engine.getEventBus());
        aiSystem.setMap(map);

        engine.addSystem(new InputSystem(engine.getEntityManager(), engine.getEventBus(), renderSystem.getCamera()));
        engine.addSystem(new PathfindingSystem(engine.getEntityManager(), map)); 
        engine.addSystem(aiSystem);
        engine.addSystem(new SteeringSystem(engine.getEntityManager())); 
        engine.addSystem(new CombatSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory, configService, map));
        engine.addSystem(new ProjectileSystem(engine.getEntityManager()));
        engine.addSystem(new ParticleUpdateSystem(engine.getEntityManager()));
        engine.addSystem(new MovementSystem(engine.getEntityManager(), map)); 
        engine.addSystem(new MapSystem(engine.getEntityManager(), map, engine.getEventBus()));
        engine.addSystem(new TriggerSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new CollisionSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new DamageSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new SoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService));
        engine.addSystem(new AmbientSoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService));
        engine.addSystem(new MultiKillSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new TauntSystem(engine.getEntityManager(), engine.getEventBus(), audioService));
        engine.addSystem(new AnimationSystem(engine.getEntityManager()));
        engine.addSystem(new WaveSystem(engine.getEntityManager(), entityFactory, map));
        engine.addSystem(renderSystem);
        engine.addSystem(uiSystem);

        mapService.spawnEntities(mapConfig);

        List<Entity> players = engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class);
        if (!players.isEmpty()) {
            Entity player = players.get(0);
            engine.getEntityManager().addComponent(player, new LightComponent(200f, new Color(1, 0.9f, 0.7f, 1f), 0.8f));
            
            InventoryComponent inv = new InventoryComponent();
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

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            gsm.push(new PauseState(gsm));
            return;
        }

        if (isGameOver) {
            RenderSystem rs = null;
            UISystem ui = null;
            for (pl.shooter.engine.ecs.GameSystem system : engine.getSystems()) {
                if (system instanceof RenderSystem) rs = (RenderSystem) system;
                if (system instanceof UISystem) ui = (UISystem) system;
            }
            if (rs != null) rs.update(deltaTime);
            if (ui != null) ui.update(deltaTime);

            if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) resetState();
            return;
        }

        engine.update(deltaTime);
        checkGameOver();
    }

    private void checkGameOver() {
        List<Entity> players = engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class);
        if (players.isEmpty()) {
            isGameOver = true;
            return;
        }
        
        HealthComponent health = engine.getEntityManager().getComponent(players.get(0), HealthComponent.class);
        if (health != null && health.isDead) {
            isGameOver = true;
        }
    }

    @Override public void render() {}
    @Override public void resize(int width, int height) { if (engine != null) engine.resize(width, height); }
    @Override public void dispose() {
        if (engine != null) engine.dispose();
        if (assetService != null) assetService.dispose();
        if (audioService != null) audioService.dispose();

        if (config != null && config.ui.useCustomCursor) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }
}
