package pl.shooter.engine.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import pl.shooter.engine.ecs.components.AnimationComponent;

/**
 * Renders characters using sprite animations.
 */
public class SpriteCharacterRenderer implements CharacterRenderer {
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float x, float y, float rotation, AnimationComponent anim, Color tint) {
        if (batch == null || anim == null) return;
        
        TextureRegion frame = anim.getCurrentKeyFrame();
        if (frame != null) {
            batch.setColor(tint);
            // Draw centered, with rotation (compensating for 90 deg offset in assets)
            batch.draw(frame, 
                x - anim.width / 2, y - anim.height / 2, 
                anim.width / 2, anim.height / 2, 
                anim.width, anim.height, 
                1, 1, rotation - 90);
        }
    }
}
