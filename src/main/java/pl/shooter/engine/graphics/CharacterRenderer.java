package pl.shooter.engine.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import pl.shooter.engine.ecs.components.AnimationComponent;

/**
 * Strategy interface for character rendering.
 * Can be implemented as Sprite-based or Procedural-based.
 */
public interface CharacterRenderer {
    /**
     * Renders the character. 
     * Implementations should check if the required tool (batch or shapeRenderer) is non-null.
     */
    void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float x, float y, float rotation, AnimationComponent anim, Color tint);
}
