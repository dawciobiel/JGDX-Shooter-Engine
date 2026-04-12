package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

    public RenderSystem(EntityManager entityManager, AssetService assetService) {
        super(entityManager);
        this.assetService = assetService;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        // Use ExtendViewport for game world to see more when window is larger
        this.viewport = new ExtendViewport(800, 600, camera);
    }

    public void setMap(GameMap map) {
        this.currentMap = map;
    }

    @Override
    public void update(float deltaTime) {
        updateCamera();
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
        spriteBatch.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderPrimitiveEntities() {
        List<Entity> entities = entityManager.getEntitiesWithComponents(TransformComponent.class, RenderComponent.class);
        for (Entity entity : entities) {
            if (entityManager.hasComponent(entity, TextureComponent.class)) continue;
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
                spriteBatch.draw(texture, t.x - tex.width / 2, t.y - tex.height / 2, tex.width / 2, tex.height / 2, tex.width, tex.height, 1, 1, t.rotation, 0, 0, texture.getWidth(), texture.getHeight(), false, false);
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
    }

    public OrthographicCamera getCamera() { return camera; }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
