package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.ProceduralMap;

public class PlayState extends GameState {
    private final Engine engine;
    private final AssetService assetService;
    private final AudioService audioService;
    private final EntityFactory entityFactory;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        this.engine = new Engine();
        this.assetService = new AssetService();
        this.audioService = new AudioService();
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService);

        init();
    }

    private void init() {
        // --- 1. Load Player Assets ---
        assetService.loadTexture("assets/2dpixx_-_free_2d_topdown_shooter_pack/2DPIXX - Free Topdown Shooter - Soldier - Walk.png");

        // --- 2. Load Zombie Assets ---
        for (int i = 0; i <= 16; i++) {
            assetService.loadTexture("assets/tds_zombie/skeleton-idle_" + i + ".png");
            assetService.loadTexture("assets/tds_zombie/skeleton-move_" + i + ".png");
        }
        for (int i = 0; i <= 8; i++) {
            assetService.loadTexture("assets/tds_zombie/skeleton-attack_" + i + ".png");
        }

        assetService.finishLoading();

        try {
            audioService.loadSound("shoot", "assets/sfx/shoot.wav");
            audioService.loadSound("hit", "assets/sfx/hit.wav");
        } catch (Exception e) {
            Gdx.app.log("PlayState", "Sound files not found.");
        }

        ProceduralMap map = new ProceduralMap();
        RenderSystem renderSystem = new RenderSystem(engine.getEntityManager(), assetService);
        renderSystem.setMap(map);

        engine.addSystem(new InputSystem(engine.getEntityManager(), engine.getEventBus(), renderSystem.getCamera()));
        engine.addSystem(new AISystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new CombatSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new ProjectileSystem(engine.getEntityManager()));
        engine.addSystem(new ParticleUpdateSystem(engine.getEntityManager()));
        engine.addSystem(new MovementSystem(engine.getEntityManager()));
        engine.addSystem(new MapSystem(engine.getEntityManager(), map));
        engine.addSystem(new CollisionSystem(engine.getEntityManager(), engine.getEventBus()));
        engine.addSystem(new DamageSystem(engine.getEntityManager(), engine.getEventBus(), entityFactory));
        engine.addSystem(new SoundSystem(engine.getEntityManager(), engine.getEventBus(), audioService));
        engine.addSystem(new AnimationSystem(engine.getEntityManager()));
        engine.addSystem(renderSystem);
        engine.addSystem(new UISystem(engine.getEntityManager()));

        // --- SPAWN DATA-DRIVEN ---
        entityFactory.loadFromJson("assets/entities/player.json", 400, 300);
        entityFactory.loadFromJson("assets/entities/zombie.json", 100, 100);
        entityFactory.loadFromJson("assets/entities/zombie.json", 700, 500);
    }

    @Override
    public void update(float deltaTime) {
        engine.update(deltaTime);
    }

    @Override
    public void render() {}

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void dispose() {
        engine.dispose();
        assetService.dispose();
        audioService.dispose();
    }
}
