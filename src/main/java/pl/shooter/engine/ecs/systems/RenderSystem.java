package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.ProceduralMap;

import java.util.List;

public class RenderSystem extends GameSystem {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final AssetService assetService;
    private GameMap currentMap;

    // Lighting
    private FrameBuffer sceneFbo;
    private ShaderProgram lightingShader;
    private LightSystem lightSystem;

    public RenderSystem(EntityManager entityManager, AssetService assetService) {
        super(entityManager);
        this.assetService = assetService;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(800, 600, camera);

        initShaders();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void initShaders() {
        String vert = Gdx.files.internal("assets/shaders/lighting.vert").readString();
        String frag = Gdx.files.internal("assets/shaders/lighting.frag").readString();
        this.lightingShader = new ShaderProgram(vert, frag);
        if (!lightingShader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed: " + lightingShader.getLog());
        }
    }

    public void setMap(GameMap map) {
        this.currentMap = map;
    }

    public void setLightSystem(LightSystem lightSystem) {
        this.lightSystem = lightSystem;
    }

    @Override
    public void update(float deltaTime) {
        updateCamera();

        // 1. Start rendering the game scene to FBO
        if (sceneFbo != null) {
            sceneFbo.begin();
            ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (currentMap instanceof ProceduralMap pMap) {
                renderProceduralMap(pMap);
            }
            renderPrimitiveEntities();
            renderHealthBars();
            shapeRenderer.end();

            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            renderTexturedEntities();
            renderAnimatedEntities();
            spriteBatch.end();
            sceneFbo.end();
        }

        // 2. Prepare lightmap
        if (lightSystem != null) {
            lightSystem.setProjectionMatrix(camera.combined);
            lightSystem.update(deltaTime);
        }

        // 3. Final Pass
        renderFinalPass();
    }

    private void renderFinalPass() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.setProjectionMatrix(spriteBatch.getProjectionMatrix().idt());
        spriteBatch.setShader(lightingShader);
        spriteBatch.begin();

        if (lightSystem != null) {
            lightSystem.getLightMapTexture().bind(1);
            lightingShader.setUniformi("u_lightmap", 1);
            lightingShader.setUniformf("u_ambientColor", lightSystem.getAmbientColor());
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }

        if (sceneFbo != null) {
            spriteBatch.draw(sceneFbo.getColorBufferTexture(), -1, 1, 2, -2);
        }
        
        spriteBatch.end();
        spriteBatch.setShader(null);
    }

    private void renderPrimitiveEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, RenderComponent.class);
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, TextureComponent.class) || 
                entityManager.hasComponent(entity, AnimationComponent.class)) continue;

            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            RenderComponent r = entityManager.getComponent(entity, RenderComponent.class);
            shapeRenderer.setColor(r.color);
            if (r.isCircle) shapeRenderer.circle(t.x, t.y, r.radius);
            else shapeRenderer.rect(t.x - r.radius, t.y - r.radius, r.radius * 2, r.radius * 2);
        }
    }

    private void renderTexturedEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, TextureComponent.class);
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            TextureComponent tex = entityManager.getComponent(entity, TextureComponent.class);
            Texture texture = assetService.getTexture(tex.assetPath);

            if (texture != null) {
                spriteBatch.setColor(Color.WHITE);
                spriteBatch.draw(texture, 
                    t.x - tex.width / 2, t.y - tex.height / 2, 
                    tex.width / 2, tex.height / 2, 
                    tex.width, tex.height, 
                    1, 1, t.rotation, 
                    0, 0, texture.getWidth(), texture.getHeight(), 
                    false, false);
            }
        }
    }

    private void renderAnimatedEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, AnimationComponent.class);
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            
            TextureRegion frame = anim.getCurrentKeyFrame();
            if (frame != null) {
                spriteBatch.draw(frame, 
                    t.x - anim.width / 2, t.y - anim.height / 2, 
                    anim.width / 2, anim.height / 2, 
                    anim.width, anim.height, 
                    1, 1, t.rotation);
            }
        }
    }

    private void renderHealthBars() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, HealthComponent.class);
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            float radius = 20f;
            if (entityManager.hasComponent(entity, RenderComponent.class)) {
                radius = entityManager.getComponent(entity, RenderComponent.class).radius;
            } else if (entityManager.hasComponent(entity, AnimationComponent.class)) {
                radius = entityManager.getComponent(entity, AnimationComponent.class).width / 2;
            }
            drawHealthBar(t.x, t.y + radius + 10, radius * 2, h);
        }
    }

    private void drawHealthBar(float x, float y, float width, HealthComponent health) {
        float barHeight = 4f;
        float halfWidth = width / 1.2f;
        shapeRenderer.setColor(1, 0, 0, 0.7f);
        shapeRenderer.rect(x - halfWidth, y, halfWidth * 2, barHeight);
        shapeRenderer.setColor(0, 1, 0, 0.8f);
        shapeRenderer.rect(x - halfWidth, y, halfWidth * 2 * (health.hp / health.maxHp), barHeight);
    }

    private void updateCamera() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (!players.isEmpty()) {
            TransformComponent tc = entityManager.getComponent(players.get(0), TransformComponent.class);
            camera.position.set(tc.x, tc.y, 0);
            camera.update();
        }
    }

    private void renderProceduralMap(ProceduralMap map) {
        float startX = camera.position.x - viewport.getWorldWidth() / 2 - ProceduralMap.TILE_SIZE;
        float startY = camera.position.y - viewport.getWorldHeight() / 2 - ProceduralMap.TILE_SIZE;
        float endX = camera.position.x + viewport.getWorldWidth() / 2 + ProceduralMap.TILE_SIZE;
        float endY = camera.position.y + viewport.getWorldHeight() / 2 + ProceduralMap.TILE_SIZE;

        for (float x = startX; x < endX; x += ProceduralMap.TILE_SIZE) {
            for (float y = startY; y < endY; y += ProceduralMap.TILE_SIZE) {
                float gridX = (float) Math.floor(x / ProceduralMap.TILE_SIZE) * ProceduralMap.TILE_SIZE;
                float gridY = (float) Math.floor(y / ProceduralMap.TILE_SIZE) * ProceduralMap.TILE_SIZE;
                float speed = map.getSpeedMultiplier(gridX + 1, gridY + 1);
                boolean isWalkable = map.isWalkable(gridX, gridY);
                if (!isWalkable) shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
                else if (speed < 0.4f) shapeRenderer.setColor(0.1f, 0.2f, 0.4f, 1);
                else if (speed < 0.7f) shapeRenderer.setColor(0.25f, 0.15f, 0.05f, 1);
                else shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
                shapeRenderer.rect(gridX, gridY, ProceduralMap.TILE_SIZE, ProceduralMap.TILE_SIZE);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        if (sceneFbo != null) sceneFbo.dispose();
        sceneFbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        if (lightSystem != null) lightSystem.resize(width, height);
    }

    public OrthographicCamera getCamera() { return camera; }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        lightingShader.dispose();
        if (sceneFbo != null) sceneFbo.dispose();
    }
}
