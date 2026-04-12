package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.ProceduralMap;

public class PlayState extends GameState {
    private final Engine engine;
    private final AssetService assetService;
    private final AudioService audioService;
    private final EntityFactory entityFactory;
    private final EntityManager entityManager;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        this.engine = new Engine();
        this.entityManager = engine.getEntityManager();
        this.assetService = new AssetService();
        this.audioService = new AudioService();
        this.entityFactory = new EntityFactory(engine.getEntityManager());

        init();
    }

    private void init() {
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
        engine.addSystem(renderSystem);
        engine.addSystem(new UISystem(engine.getEntityManager()));

        // --- SPAWN ---
        entityFactory.createPlayer(400, 300);
        
        // Use full path from project root
        entityFactory.loadFromJson("assets/entities/zombie.json", 100, 100);
        entityFactory.loadFromJson("assets/entities/zombie.json", 700, 500);
    }

    @Override
    public void update(float deltaTime) {
        engine.update(deltaTime);
    }

    @Override
    public void render() {
    }

    @Override
    public void dispose() {
        engine.dispose();
        assetService.dispose();
        audioService.dispose();
    }
}
