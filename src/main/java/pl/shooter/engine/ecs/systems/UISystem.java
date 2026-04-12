package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.HealthComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.ScoreComponent;
import pl.shooter.engine.ecs.components.WeaponComponent;

import java.util.List;

/**
 * Handles the On-Screen Display (HUD).
 * Renders player health, score, and weapon/ammo info.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;

    public UISystem(EntityManager entityManager) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont(); // Default LibGDX font
        this.font.getData().setScale(1.2f);
    }

    @Override
    public void update(float deltaTime) {
        // UI rendering should not be affected by game camera
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class);
        
        if (players.isEmpty()) {
            renderGameOver();
            return;
        }

        Entity player = players.get(0);
        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
        ScoreComponent score = entityManager.getComponent(player, ScoreComponent.class);
        WeaponComponent weapon = entityManager.getComponent(player, WeaponComponent.class);

        renderHUD(health, score, weapon);
    }

    private void renderHUD(HealthComponent health, ScoreComponent score, WeaponComponent weapon) {
        // 1. Draw Health Bar
        float x = 20;
        float y = Gdx.graphics.getHeight() - 40;
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Fill
        if (health != null) {
            float healthPercent = Math.max(0, Math.min(1, health.hp / health.maxHp));
            Color healthColor = Color.GREEN;
            if (healthPercent < 0.6f) healthColor = Color.YELLOW;
            if (healthPercent < 0.3f) healthColor = Color.RED;
            
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);
        }
        shapeRenderer.end();

        // 2. Draw Text Info
        batch.begin();
        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HEALTH: " + (int)health.hp + " / " + (int)health.maxHp, x, y + barHeight + 20);
        }

        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, x, y - 15);
        }

        if (weapon != null) {
            font.setColor(Color.CYAN);
            String ammoText = weapon.hasInfiniteAmmo ? "INF" : weapon.currentAmmo + " / " + weapon.maxAmmo;
            font.draw(batch, "WEAPON: " + weapon.type + " [" + ammoText + "]", 
                     Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 20);
        }
        batch.end();
    }

    private void renderGameOver() {
        batch.begin();
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", Gdx.graphics.getWidth()/2f - 60, Gdx.graphics.getHeight()/2f + 20);
        font.setColor(Color.WHITE);
        font.draw(batch, "Press any key to restart", Gdx.graphics.getWidth()/2f - 110, Gdx.graphics.getHeight()/2f - 10);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
