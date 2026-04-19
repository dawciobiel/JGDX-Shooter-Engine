package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.LightComponent;
import pl.shooter.engine.ecs.components.TransformComponent;

import java.util.List;

/**
 * Manages the dynamic lighting pass by rendering radial lights into a FrameBuffer.
 * Uses a shared SpriteBatch for better performance.
 */
public class LightSystem extends GameSystem {
    private FrameBuffer lightMap;
    private final SpriteBatch batch;
    private final Texture lightTexture;
    private final Color ambientColor = new Color(0.1f, 0.1f, 0.2f, 0.5f);
    private final Matrix4 projectionMatrix = new Matrix4();

    public LightSystem(EntityManager entityManager, SpriteBatch batch) {
        super(entityManager);
        this.batch = batch;
        this.lightTexture = createRadialGradient();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private Texture createRadialGradient() {
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float centerX = size / 2f, centerY = size / 2f, maxDist = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                float alpha = Math.max(0, 1 - (dist / maxDist));
                pixmap.setColor(1, 1, 1, alpha * alpha);
                pixmap.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return tex;
    }

    @Override
    public void update(float deltaTime) {
        if (lightMap == null) return;
        List<Entity> lights = entityManager.getEntitiesWithComponents(TransformComponent.class, LightComponent.class);
        
        lightMap.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(projectionMatrix);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        for (Entity entity : lights) {
            TransformComponent t = entityManager.getComponent(entity, TransformComponent.class);
            LightComponent lc = entityManager.getComponent(entity, LightComponent.class);
            batch.setColor(lc.color.r, lc.color.g, lc.color.b, lc.intensity);
            float d = lc.radius * 2;
            batch.draw(lightTexture, t.x - lc.radius, t.y - lc.radius, d, d);
        }
        batch.end();
        
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        lightMap.end();
    }

    public void setAmbientColor(float r, float g, float b, float a) {
        this.ambientColor.set(
            MathUtils.clamp(r, 0, 1),
            MathUtils.clamp(g, 0, 1),
            MathUtils.clamp(b, 0, 1),
            MathUtils.clamp(a, 0, 1)
        );
    }

    public void setProjectionMatrix(Matrix4 matrix) { this.projectionMatrix.set(matrix); }
    public Texture getLightMapTexture() { return lightMap.getColorBufferTexture(); }
    public Color getAmbientColor() { return ambientColor; }

    @Override
    public void resize(int width, int height) {
        if (lightMap != null) lightMap.dispose();
        lightMap = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    @Override
    public void dispose() {
        if (lightMap != null) lightMap.dispose();
        lightTexture.dispose();
    }
}
