package pl.shooter.engine.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import pl.shooter.engine.config.models.CharacterPrefab;
import pl.shooter.engine.ecs.components.AnimationComponent;

/**
 * Renders characters using geometric shapes.
 */
public class ProceduralCharacterRenderer implements CharacterRenderer {
    private final Color primaryColor;
    private final Color accentColor;
    private final float baseRadius;

    public ProceduralCharacterRenderer(CharacterPrefab.ProceduralConfig config) {
        if (config != null) {
            this.primaryColor = Color.valueOf(config.primaryColor);
            this.accentColor = Color.valueOf(config.accentColor);
            this.baseRadius = config.radius;
        } else {
            this.primaryColor = Color.WHITE;
            this.accentColor = Color.RED;
            this.baseRadius = 16f;
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float x, float y, float rotation, AnimationComponent anim, Color tint) {
        if (shapeRenderer == null) return;

        float alpha = tint.a;
        float bob = 0;
        float pulse = 1.0f;

        // Apply visual state feedback
        if (anim != null) {
            if (anim.currentState == AnimationComponent.State.WALK) {
                bob = MathUtils.sin(anim.stateTime * 15f) * 3f;
            } else if (anim.currentState == AnimationComponent.State.SHOOT) {
                pulse = 1.2f;
            }
        }

        float radius = (baseRadius + bob) * pulse;

        // Draw main body
        shapeRenderer.setColor(primaryColor.r, primaryColor.g, primaryColor.b, primaryColor.a * alpha);
        shapeRenderer.circle(x, y, radius);

        // Draw direction indicator (eyes/weapon)
        shapeRenderer.setColor(accentColor.r, accentColor.g, accentColor.b, accentColor.a * alpha);
        float dirX = MathUtils.cosDeg(rotation);
        float dirY = MathUtils.sinDeg(rotation);
        
        // Small "head" or "front" indicator
        shapeRenderer.circle(x + dirX * (radius * 0.7f), y + dirY * (radius * 0.7f), radius * 0.4f);
        
        // Weapon-like line
        shapeRenderer.rectLine(x, y, x + dirX * radius * 1.5f, y + dirY * radius * 1.5f, 4f);
    }
}
