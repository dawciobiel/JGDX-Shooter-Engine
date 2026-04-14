package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.components.LightComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.TestingMap;

public class PlayState extends GameState {
    private Engine engine;
    private AssetService assetService;
    private AudioService audioService;
    private ConfigService configService;
    private EntityFactory entityFactory;
    private boolean isGameOver = false;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        resetState();
    }

    private void resetState() {
        if (engine != null) dispose();

        this.engine = new Engine();
        this.assetService = new AssetService();
        this.audioService = new AudioService();
        this.configService = new ConfigService();
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService);
        this.isGameOver = false;

        init();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void init() {
        assetService.loadTexture("assets/2dpixx_-_free_2d_topdown_shooter_pack/2DPIXX - Free Topdown Shooter - Soldier - Walk.png");
        for (int i = 0; i <= 16; i++) {
            assetService.loadTexture("assets/tds_zombie/skeleton-idle_" + i + ".png");
            assetService.loadTexture("assets/tds_zombie/skeleton-move_" + i + ".png");
        }
        for (int i = 0; i <= 8; i++) assetService.loadTexture("assets/tds_zombie/skeleton-attack_" + i + ".png");
        assetService.finishLoading();

        audioService.loadSound("assets/sfx/shotgun.wav");
        audioService.loadSound("assets/sfx/hit.wav");
        audioService.loadSound("assets/sfx/shoot.wav");

        GameConfig config = configService.getConfig();
        GameMap map = new TestingMap();
        
        RenderSystem renderSystem = new RenderSystem(engine.getEntityManager(), assetService);
        renderSystem.setMap(map);
        renderSystem.setShowDebugPaths(config.debug.showPaths); // Apply config

        LightSystem lightSystem = new LightSystem(engine.getEntityManager());
        lightSystem.setAmbientColor(config.graphics.ambientRed, config.graphics.ambientGreen, config.graphics.ambientBlue, config.graphics.ambientBrightness);
        renderSystem.setLightSystem(lightSystem);

        engine.addSystem(new InputSystem(engine.getEntityManager(), engine.getEventBus(), renderSystem.getCamera()));
        engine.addSystem(new PathfindingSystem(engine.getEntityManager(), map)); 
        engine.addSystem(new AISystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new SteeringSystem(engine.getEntityManager())); 
        engine.addSystem(new CombatSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new ProjectileSystem(engine.getEntityManager()));
        engine.addSystem(new ParticleUpdateSystem(engine.getEntityManager()));
        engine.addSystem(new MovementSystem(engine.getEntityManager(), map)); 
        engine.addSystem(new MapSystem(engine.getEntityManager(), map));
        engine.addSystem(new CollisionSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new DamageSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new SoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService));
        engine.addSystem(new AnimationSystem(engine.getEntityManager()));
        engine.addSystem(new WaveSystem(engine.getEntityManager(), entityFactory, map));
        engine.addSystem(renderSystem);
        engine.addSystem(new UISystem(engine.getEntityManager()));

        // Spawn player
        Entity player = entityFactory.loadFromJson("assets/entities/player.json", 200, 200);
        if (player != null) {
            engine.getEntityManager().addComponent(player, new LightComponent(200f, new Color(1, 0.9f, 0.7f, 1f), 0.8f));
        }

        // Initial zombies
        for (int i = 0; i < 5; i++) {
            float zx, zy;
            do {
                zx = MathUtils.random(100, 1400);
                zy = MathUtils.random(100, 1400);
            } while (!map.isWalkable(zx, zy));
            entityFactory.loadFromJson("assets/entities/zombie.json", zx, zy);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            gsm.push(new PauseState(gsm));
            return;
        }

        if (isGameOver) {
            engine.getSystems().forEach(system -> {
                if (system instanceof RenderSystem || system instanceof UISystem) system.update(deltaTime);
            });
            if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) resetState();
            return;
        }

        engine.update(deltaTime);
        if (engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class).isEmpty()) isGameOver = true;
    }

    @Override public void render() {}
    @Override public void resize(int width, int height) { if (engine != null) engine.resize(width, height); }
    @Override public void dispose() {
        if (engine != null) engine.dispose();
        if (assetService != null) assetService.dispose();
        if (audioService != null) audioService.dispose();
    }
}
