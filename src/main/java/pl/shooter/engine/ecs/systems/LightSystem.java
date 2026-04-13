package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.LightComponent;
import pl.shooter.engine.ecs.components.TransformComponent;

import java.util.List;

public class LightSystem extends GameSystem {
    private FrameBuffer lightMap;
    private final SpriteBatch batch;
    private final Texture lightTexture;
    private final Color ambientColor = new Color(0.1f, 0.1f, 0.2f, 0.5f);

    public LightSystem(EntityManager entityManager) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.lightTexture = createRadialGradient(128);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private Texture createRadialGradient(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float centerX = size / 2f;
        float centerY = size / 2f;
        float maxDist = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = Vector2.dst(x, y, centerX, centerY);
                float alpha = Math.max(0, 1 - (dist / maxDist));
                alpha = alpha * alpha; 
                pixmap.setColor(1, 1, 1, alpha);
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
        lightMap.end();
    }

    /**
     * Sets the global ambient light color and intensity.
     * Alpha channel controls overall brightness (0 = dark, 1 = bright).
     */
    public void setAmbientColor(float r, float g, float b, float a) {
        this.ambientColor.set(r, g, b, a);
    }

    public void setProjectionMatrix(Matrix4 matrix) {
        batch.setProjectionMatrix(matrix);
    }

    public Texture getLightMapTexture() {
        return lightMap.getColorBufferTexture();
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    @Override
    public void resize(int width, int height) {
        if (lightMap != null) lightMap.dispose();
        lightMap = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    @Override
    public void dispose() {
        if (lightMap != null) lightMap.dispose();
        batch.dispose();
        lightTexture.dispose();
    }
}
