package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.models.EngineConfig;
import pl.shooter.engine.config.models.GameplayConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.graphics.CharacterRenderer;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.JsonMap;

import java.util.List;

/**
 * Renders the game world. 
 * Updated to use new Asset Architecture and EngineConfig.
 */
public class RenderSystem extends GameSystem {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final AssetService assetService;
    private EngineConfig config;
    private GameplayConfig gameplayConfig;
    private GameMap currentMap;
    private final BitmapFont nameFont;
    private final Vector3 mouseBuffer = new Vector3();

    private FrameBuffer sceneFbo;
    private ShaderProgram lightingShader;
    private LightSystem lightSystem;
    private boolean showDebugPaths = false;
    private boolean showDebugHitboxes = false;

    private TextureRegion[][] cachedTiles;
    private String lastTilesetPath;

    public RenderSystem(EntityManager entityManager, AssetService assetService, SpriteBatch batch, ShapeRenderer shapeRenderer) {
        super(entityManager);
        this.assetService = assetService;
        this.spriteBatch = batch;
        this.shapeRenderer = shapeRenderer;
        this.camera = new OrthographicCamera();
        this.viewport = new ExtendViewport(800, 600, camera);
        this.nameFont = new BitmapFont();
        this.nameFont.getData().setScale(0.8f);
    }

    public void init(EngineConfig engineConfig, GameplayConfig gameplayConfig) {
        this.config = engineConfig;
        this.gameplayConfig = gameplayConfig;
        initShaders(engineConfig);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void initShaders(EngineConfig config) {
        String shaderBase = config.paths.globalRoot + "/" + config.paths.shaders + "/";
        String vert = Gdx.files.internal(shaderBase + "lighting.vert").readString();
        String frag = Gdx.files.internal(shaderBase + "lighting.frag").readString();
        this.lightingShader = new ShaderProgram(vert, frag);
        if (!lightingShader.isCompiled()) {
            Gdx.app.error("RenderSystem", "Shader compilation failed: " + lightingShader.getLog());
        }
    }

    public void setMap(GameMap map) { this.currentMap = map; this.cachedTiles = null; }
    public void setLightSystem(LightSystem lightSystem) { this.lightSystem = lightSystem; }
    public void setShowDebugPaths(boolean show) { this.showDebugPaths = show; }
    public void setShowDebugHitboxes(boolean show) { this.showDebugHitboxes = show; }

    @Override
    public void update(float deltaTime) {
        updateCamera();

        List<Entity> texturedEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, TextureComponent.class);
        List<Entity> animatedEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, AnimationComponent.class);
        List<Entity> primitiveEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, RenderComponent.class);
        List<Entity> hybridEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, CharacterRendererComponent.class);
        List<Entity> healthEntities = entityManager.getEntitiesWithComponents(HealthComponent.class, TransformComponent.class);
        List<Entity> namedEntities = entityManager.getEntitiesWithComponents(TransformComponent.class, NameComponent.class);

        if (sceneFbo != null) {
            sceneFbo.begin();
            ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            if (currentMap instanceof JsonMap jm && jm.getTilesetPath() != null) {
                spriteBatch.setProjectionMatrix(camera.combined);
                spriteBatch.begin();
                spriteBatch.setColor(Color.WHITE);
                renderJsonMap(jm);
                spriteBatch.end();
            } else {
                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                if (currentMap != null) renderMapLegacy(currentMap);
                shapeRenderer.end();
            }

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            renderBloodDecals(healthEntities);
            shapeRenderer.end();

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            renderPrimitiveEntities(primitiveEntities);
            renderHybridEntities(hybridEntities, null, shapeRenderer);
            renderHealthBars(healthEntities);
            shapeRenderer.end();

            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            renderTexturedEntities(texturedEntities);
            renderAnimatedEntities(animatedEntities);
            renderHybridEntities(hybridEntities, spriteBatch, null);
            if (gameplayConfig != null && gameplayConfig.showUnitNames) {
                renderUnitNames(namedEntities);
            }
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

    private void renderHybridEntities(List<Entity> entities, SpriteBatch batch, ShapeRenderer shapes) {
        for (Entity entity : entities) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            CharacterRendererComponent crc = entityManager.getComponent(entity, CharacterRendererComponent.class);
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            crc.renderer.render(batch, shapes, t.x, t.y, t.rotation, anim, getEntityTint(entity));
        }
    }

    private void renderJsonMap(JsonMap map) {
        int[][] data = map.getTileData();
        if (data == null) return;
        int ts = map.getTileSize();
        int ds = map.getDisplaySize();
        
        Texture tileset = assetService.getTexture(map.getTilesetPath());
        if (tileset == null) return;

        if (cachedTiles == null || !map.getTilesetPath().equals(lastTilesetPath)) {
            cachedTiles = TextureRegion.split(tileset, ts, ts);
            lastTilesetPath = map.getTilesetPath();
        }

        float startX = camera.position.x - viewport.getWorldWidth() / 2 - ds;
        float startY = camera.position.y - viewport.getWorldHeight() / 2 - ds;
        float endX = camera.position.x + viewport.getWorldWidth() / 2 + ds;
        float endY = camera.position.y + viewport.getWorldHeight() / 2 + ds;
        int startGridX = Math.max(0, (int) (startX / ds));
        int startGridY = Math.max(0, (int) (startY / ds));
        int endGridX = Math.min(data[0].length, (int) (endX / ds) + 1);
        int endGridY = Math.min(data.length, (int) (endY / ds) + 1);

        int colsInTileset = tileset.getWidth() / ts;
        for (int y = startGridY; y < endGridY; y++) {
            for (int x = startGridX; x < endGridX; x++) {
                int tileId = data[y][x];
                int row = tileId / colsInTileset;
                int col = tileId % colsInTileset;
                if (row >= 0 && row < cachedTiles.length && col >= 0 && col < cachedTiles[0].length) {
                    spriteBatch.draw(cachedTiles[row][col], x * ds, y * ds, ds, ds);
                }
            }
        }
    }

    private void renderMapLegacy(GameMap map) {
        float ts = 32f;
        float startX = camera.position.x - viewport.getWorldWidth() / 2 - ts;
        float startY = camera.position.y - viewport.getWorldHeight() / 2 - ts;
        float endX = camera.position.x + viewport.getWorldWidth() / 2 + ts;
        float endY = camera.position.y + viewport.getWorldHeight() / 2 + ts;
        for (float x = startX; x < endX; x += ts) {
            for (float y = startY; y < endY; y += ts) {
                float wx = (float)Math.floor(x / ts) * ts;
                float wy = (float)Math.floor(y / ts) * ts;
                float speed = map.getSpeedMultiplier(wx + ts/2, wy + ts/2);
                if (!map.isWalkable(wx + ts/2, wy + ts/2)) shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
                else if (speed < 0.4f) shapeRenderer.setColor(0.1f, 0.2f, 0.4f, 1);
                else shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1);
                shapeRenderer.rect(wx, wy, ts, ts);
            }
        }
    }

    private void renderBloodDecals(List<Entity> healthEntities) {
        for (Entity entity : healthEntities) {
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            if (h.isDead && h.hasBlood) {
                TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
                float alpha = 0.6f;
                if (!h.corpseStayPermanent) {
                    alpha *= (1.0f - (h.deathTimer / h.corpseDuration));
                }
                shapeRenderer.setColor(h.bloodColor.r, h.bloodColor.g, h.bloodColor.b, h.bloodColor.a * alpha);
                shapeRenderer.circle(t.x, t.y, h.bloodSize);
            }
        }
    }

    private void renderPrimitiveEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, TextureComponent.class) || 
                entityManager.hasComponent(entity, AnimationComponent.class) ||
                entityManager.hasComponent(entity, CharacterRendererComponent.class)) continue;
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            RenderComponent r = entityManager.getComponent(entity, RenderComponent.class);
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            float alpha = (h != null && h.isDead) ? (1.0f - (h.deathTimer / h.corpseDuration)) : 1.0f;
            shapeRenderer.setColor(r.color.r, r.color.g, r.color.b, r.color.a * alpha);
            if (r.isCircle) shapeRenderer.circle(t.x, t.y, r.radius);
            else shapeRenderer.rect(t.x - r.radius, t.y - r.radius, r.radius * 2, r.radius * 2);
        }
    }

    private void renderTexturedEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, AnimationComponent.class) ||
                entityManager.hasComponent(entity, CharacterRendererComponent.class)) continue;
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            TextureComponent tex = entityManager.getComponent(entity, TextureComponent.class);
            Texture texture = assetService.getTexture(tex.assetPath);
            if (texture == null) {
                assetService.loadTexture(tex.assetPath);
                texture = assetService.getTexture(tex.assetPath);
            }
            if (texture != null) {
                spriteBatch.setColor(getEntityTint(entity));
                spriteBatch.draw(texture, t.x - tex.width / 2, t.y - tex.height / 2, tex.width / 2, tex.height / 2, tex.width, tex.height, 1, 1, t.rotation - 90, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
            }
        }
    }

    private void renderAnimatedEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, CharacterRendererComponent.class)) continue;
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            AnimationComponent anim = entityManager.getComponent(entity, AnimationComponent.class);
            TextureRegion frame = anim.getCurrentKeyFrame();
            if (frame != null) {
                spriteBatch.setColor(getEntityTint(entity));
                spriteBatch.draw(frame, t.x - anim.width / 2, t.y - anim.height / 2, anim.width / 2, anim.height / 2, anim.width, anim.height, 1, 1, t.rotation - 90);
            }
        }
    }

    private void renderUnitNames(List<Entity> entities) {
        for (Entity entity : entities) {
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            if (h != null && h.isDead) continue;
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            NameComponent n = entityManager.getComponent(entity, NameComponent.class);
            float offset = 40f;
            nameFont.setColor(1, 1, 1, 0.9f);
            nameFont.draw(spriteBatch, n.name, t.x - (n.name.length() * 3), t.y + offset);
        }
    }

    private Color getEntityTint(Entity entity) {
        HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
        float alpha = (h != null && h.isDead) ? Math.max(0, 1.0f - (h.deathTimer / h.corpseDuration)) : 1.0f;
        RenderComponent r = entityManager.getComponent(entity, RenderComponent.class);
        if (r != null) return new Color(r.color.r, r.color.g, r.color.b, r.color.a * alpha);
        return new Color(1, 1, 1, alpha);
    }

    private void renderHealthBars(List<Entity> healthEntities) {
        for (Entity entity : healthEntities) {
            HealthComponent h = entityManager.getComponent(entity, HealthComponent.class);
            if (h.isDead || entityManager.hasComponent(entity, PlayerComponent.class)) continue;
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            drawHealthBar(t.x, t.y + 30, h);
        }
    }

    private void drawHealthBar(float x, float y, HealthComponent health) {
        float width = 40f;
        float barHeight = 4f;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x - width/2, y, width, barHeight);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x - width/2, y, width * (health.hp / health.maxHp), barHeight);
    }

    private void updateCamera() {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (!players.isEmpty()) {
            TransformComponent tc = entityManager.getComponent(players.getFirst(), TransformComponent.class);
            camera.position.lerp(new Vector3(tc.x, tc.y, 0), 0.1f);
            camera.update();
        }
    }

    private void renderFinalPass() {
        // Use a 1:1 projection for drawing the FBO texture to the screen
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.setShader(lightingShader);
        spriteBatch.begin();
        spriteBatch.setColor(Color.WHITE);
        if (lightSystem != null) {
            lightSystem.getLightMapTexture().bind(1);
            lightingShader.setUniformi("u_lightmap", 1);
            Color amb = lightSystem.getAmbientColor();
            lightingShader.setUniformf("u_ambientColor", amb.r, amb.g, amb.b, amb.a);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        }
        if (sceneFbo != null) {
            Texture fboTexture = sceneFbo.getColorBufferTexture();
            // Draw full screen
            spriteBatch.draw(fboTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, fboTexture.getWidth(), fboTexture.getHeight(), false, true);
        }
        spriteBatch.end();
        spriteBatch.setShader(null);
    }

    private void renderDebugInfo() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (showDebugHitboxes) {
            shapeRenderer.setColor(Color.RED);
            List<Entity> colliders = entityManager.getEntitiesWithComponents(TransformComponent.class, ColliderComponent.class);
            for (Entity entity : colliders) {
                TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
                ColliderComponent c = entityManager.getComponent(entity, ColliderComponent.class);
                shapeRenderer.circle(t.x, t.y, c.radius);
            }
        }
        if (showDebugPaths) {
            shapeRenderer.setColor(Color.CYAN);
            int ts = 32;
            if (currentMap instanceof JsonMap jm) ts = jm.getDisplaySize();
            float offset = ts / 2f;

            List<Entity> aiEntities = entityManager.getEntitiesWithComponents(AIComponent.class);
            for (Entity entity : aiEntities) {
                AIComponent ai = entityManager.getComponent(entity, AIComponent.class);
                if (ai.currentPath != null && ai.currentPath.getCount() > 0) {
                    for (int i = 0; i < ai.currentPath.getCount() - 1; i++) {
                        Node n1 = ai.currentPath.get(i);
                        Node n2 = ai.currentPath.get(i + 1);
                        shapeRenderer.line(n1.x * ts + offset, n1.y * ts + offset, n2.x * ts + offset, n2.y * ts + offset);
                    }
                }
            }
        }
        shapeRenderer.end();
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
        if (lightingShader != null) lightingShader.dispose();
        if (sceneFbo != null) sceneFbo.dispose();
        if (nameFont != null) nameFont.dispose();
    }
}
