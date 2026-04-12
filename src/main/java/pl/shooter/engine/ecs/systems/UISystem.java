package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.HealthComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.ScoreComponent;

import java.util.List;

/**
 * Handles the On-Screen Display (HUD).
 * Renders player health and score.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final BitmapFont font;

    public UISystem(EntityManager entityManager) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.font = new BitmapFont(); // Default LibGDX font
        this.font.getData().setScale(1.5f);
    }

    @Override
    public void update(float deltaTime) {
        // UI rendering should not be affected by game camera
        batch.begin();
        
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class);
        if (!players.isEmpty()) {
            Entity player = players.get(0);
            HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
            ScoreComponent score = entityManager.getComponent(player, ScoreComponent.class);

            if (health != null) {
                font.setColor(Color.WHITE);
                font.draw(batch, "HP: " + (int)health.hp + " / " + (int)health.maxHp, 20, Gdx.graphics.getHeight() - 20);
            }

            if (score != null) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "SCORE: " + score.score, 20, Gdx.graphics.getHeight() - 50);
            }
        } else {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER - Press any key to restart", Gdx.graphics.getWidth()/2f - 150, Gdx.graphics.getHeight()/2f);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
