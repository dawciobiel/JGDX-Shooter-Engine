package pl.shooter.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.ecs.EntityFactory;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.systems.*;
import pl.shooter.engine.state.GameState;
import pl.shooter.engine.state.GameStateManager;
import pl.shooter.engine.world.ProceduralMap;

public class PlayState extends GameState {
    private Engine engine;
    private AssetService assetService;
    private AudioService audioService;
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
        this.entityFactory = new EntityFactory(engine.getEntityManager(), assetService);
        this.isGameOver = false;

        init();
        
        // Initial resize fix - ensures viewports are updated on start
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void init() {
        // --- 1. Load Assets ---
        assetService.loadTexture("assets/2dpixx_-_free_2d_topdown_shooter_pack/2DPIXX - Free Topdown Shooter - Soldier - Walk.png");
        for (int i = 0; i <= 16; i++) {
            assetService.loadTexture("assets/tds_zombie/skeleton-idle_" + i + ".png");
            assetService.loadTexture("assets/tds_zombie/skeleton-move_" + i + ".png");
        }
        for (int i = 0; i <= 8; i++) {
            assetService.loadTexture("assets/tds_zombie/skeleton-attack_" + i + ".png");
        }
        assetService.finishLoading();

        audioService.loadSound("assets/sfx/shoot.wav");
        audioService.loadSound("assets/sfx/hit.wav");

        // --- 2. Setup Systems ---
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

        // --- 3. Spawn Initial Entities ---
        entityFactory.loadFromJson("assets/entities/player.json", 400, 300);
        entityFactory.loadFromJson("assets/entities/zombie.json", 100, 100);
        entityFactory.loadFromJson("assets/entities/zombie.json", 700, 500);
    }

    @Override
    public void update(float deltaTime) {
        if (isGameOver) {
            // Only update render and UI systems when dead, keep the world static
            engine.getSystems().forEach(system -> {
                if (system instanceof RenderSystem || system instanceof UISystem) {
                    system.update(deltaTime);
                }
            });

            if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                resetState();
            }
            return;
        }

        engine.update(deltaTime);

        // Check for Game Over (all players dead)
        if (engine.getEntityManager().getEntitiesWithComponents(PlayerComponent.class).isEmpty()) {
            isGameOver = true;
        }
    }

    @Override
    public void render() {}

    @Override
    public void resize(int width, int height) {
        if (engine != null) engine.resize(width, height);
    }

    @Override
    public void dispose() {
        if (engine != null) engine.dispose();
        if (assetService != null) assetService.dispose();
        if (audioService != null) audioService.dispose();
    }
}
