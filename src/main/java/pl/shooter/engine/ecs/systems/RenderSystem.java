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
import pl.shooter.engine.ai.pathfinding.Node;

import java.util.List;

public class RenderSystem extends GameSystem {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final AssetService assetService;
    private GameMap currentMap;

    private FrameBuffer sceneFbo;
    private ShaderProgram lightingShader;
    private LightSystem lightSystem;
    private boolean showDebugPaths = false;
    private boolean showDebugHitboxes = false;

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

    public void setMap(GameMap map) { this.currentMap = map; }
    public void setLightSystem(LightSystem lightSystem) { this.lightSystem = lightSystem; }
    public void setShowDebugPaths(boolean show) { this.showDebugPaths = show; }
    public void setShowDebugHitboxes(boolean show) { this.showDebugHitboxes = show; }

    @Override
    public void update(float deltaTime) {
        updateCamera();

        if (sceneFbo != null) {
            sceneFbo.begin();
            ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (currentMap != null) renderMap(currentMap);
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

        if (lightSystem != null) {
            lightSystem.setProjectionMatrix(camera.combined);
            lightSystem.update(deltaTime);
        }

        renderFinalPass();
        
        if (showDebugPaths || showDebugHitboxes) {
            renderDebugInfo();
        }
    }

    private void renderDebugInfo() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        if (showDebugPaths) {
            shapeRenderer.setColor(Color.CYAN);
            List<Entity> enemies = entityManager.getEntitiesWithComponents(AIComponent.class);
            for (Entity e : enemies) {
                AIComponent ai = entityManager.getComponent(e, AIComponent.class);
                if (ai.currentPath != null && ai.currentPath.getCount() > 1) {
                    for (int i = 0; i < ai.currentPath.getCount() - 1; i++) {
                        Node n1 = ai.currentPath.get(i);
                        Node n2 = ai.currentPath.get(i+1);
                        shapeRenderer.line(n1.x * 32 + 16, n1.y * 32 + 16, n2.x * 32 + 16, n2.y * 32 + 16);
                    }
                }
            }
        }

        if (showDebugHitboxes) {
            shapeRenderer.setColor(Color.LIME);
            List<Entity> colliders = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class);
            for (Entity e : colliders) {
                TransformComponent t = entityManager.getComponent(e, TransformComponent.class);
                ColliderComponent c = entityManager.getComponent(e, ColliderComponent.class);
                shapeRenderer.circle(t.x, t.y, c.radius);
            }
        }

        shapeRenderer.end();
    }

    private void renderFinalPass() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.getProjectionMatrix().idt(); // Reset to identity for full-screen quad
        spriteBatch.setShader(lightingShader);
        spriteBatch.begin();
        if (lightSystem != null) {
            lightSystem.getLightMapTexture().bind(1);
            lightingShader.setUniformi("u_lightmap", 1);
            lightingShader.setUniformf("u_ambientColor", lightSystem.getAmbientColor());
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }
        if (sceneFbo != null) {
            Texture fboTexture = sceneFbo.getColorBufferTexture();
            spriteBatch.draw(fboTexture, -1, -1, 2, 2, 0, 0, fboTexture.getWidth(), fboTexture.getHeight(), false, true);
        }
        spriteBatch.end();
        spriteBatch.setShader(null);
    }

    private void renderPrimitiveEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, RenderComponent.class);
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, TextureComponent.class) || entityManager.hasComponent(entity, AnimationComponent.class)) continue;
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
                // Added -90 offset because texture facing UP in PNG needs to be rotated to face RIGHT (0 degrees)
                spriteBatch.draw(texture, t.x - tex.width / 2, t.y - tex.height / 2, tex.width / 2, tex.height / 2, tex.width, tex.height, 1, 1, t.rotation - 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            }
        }
    }

    private void renderAnimatedEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, AnimationComponent.class);
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            TextureRegion frame = anim.getCurrentKeyFrame();
            // Added -90 offset for animations
            if (frame != null) spriteBatch.draw(frame, t.x - anim.width / 2, t.y - anim.height / 2, anim.width / 2, anim.height / 2, anim.width, anim.height, 1, 1, t.rotation - 90);
        }
    }

    private void renderHealthBars() {
        List<Entity> enemies = entityManager.getEntitiesWithComponents(TransformComponent.class, HealthComponent.class);
        for (Entity entity : enemies) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            float radius = 20f;
            if (entityManager.hasComponent(entity, RenderComponent.class)) radius = entityManager.getComponent(entity, RenderComponent.class).radius;
            else if (entityManager.hasComponent(entity, AnimationComponent.class)) radius = entityManager.getComponent(entity, AnimationComponent.class).width / 2;
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
            camera.position.lerp(new Vector3(tc.x, tc.y, 0), 0.1f); // Smooth camera
            camera.update();
        }
    }

    private void renderMap(GameMap map) {
        float startX = camera.position.x - viewport.getWorldWidth() / 2 - 32;
        float startY = camera.position.y - viewport.getWorldHeight() / 2 - 32;
        float endX = camera.position.x + viewport.getWorldWidth() / 2 + 32;
        float endY = camera.position.y + viewport.getWorldHeight() / 2 + 32;
        for (float x = startX; x < endX; x += 32) {
            for (float y = startY; y < endY; y += 32) {
                int gx = (int) Math.floor(x / 32);
                int gy = (int) Math.floor(y / 32);
                float wx = gx * 32;
                float wy = gy * 32;
                float speed = map.getSpeedMultiplier(wx + 16, wy + 16);
                boolean isWalkable = map.isWalkable(wx + 16, wy + 16);
                if (!isWalkable) shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
                else if (speed < 0.4f) shapeRenderer.setColor(0.1f, 0.2f, 0.4f, 1);
                else if (speed < 0.7f) shapeRenderer.setColor(0.25f, 0.15f, 0.05f, 1);
                else shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
                shapeRenderer.rect(wx, wy, 32, 32);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        if (sceneFbo != null) sceneFbo.dispose();
        sceneFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        if (lightSystem != null) lightSystem.resize(width, height);
    }

    public OrthographicCamera getCamera() { return camera; }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        if (lightingShader != null) lightingShader.dispose();
        if (sceneFbo != null) sceneFbo.dispose();
    }
}
