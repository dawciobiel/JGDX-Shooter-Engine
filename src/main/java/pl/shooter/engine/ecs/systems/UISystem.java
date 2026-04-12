package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
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
 * Uses a separate viewport to ensure correct scaling on resize.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;

    public UISystem(EntityManager entityManager) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        // Fixed internal UI resolution: 800x600
        this.viewport = new FitViewport(800, 600);
    }

    @Override
    public void update(float deltaTime) {
        viewport.apply();
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
        float x = 20;
        float y = 600 - 40; // Use fixed virtual height
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 1. Health Bar
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        if (health != null) {
            float healthPercent = Math.max(0, Math.min(1, health.hp / health.maxHp));
            Color healthColor = Color.GREEN;
            if (healthPercent < 0.6f) healthColor = Color.YELLOW;
            if (healthPercent < 0.3f) healthColor = Color.RED;
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);
        }

        // 2. Reload Bar
        if (weapon != null && weapon.isReloading) {
            float ry = y - 40;
            float reloadPercent = Math.min(1, weapon.reloadTimer / weapon.reloadTime);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(x - 2, ry - 2, barWidth + 4, 14);
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(x, ry, barWidth, 10);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x, ry, barWidth * reloadPercent, 10);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HEALTH: " + (int)health.hp, x, y + barHeight + 20);
        }

        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, x, y - 15);
        }

        if (weapon != null) {
            font.setColor(Color.CYAN);
            String ammoText = weapon.hasInfiniteAmmo ? weapon.magazineAmmo + " / INF" : weapon.magazineAmmo + " / " + weapon.currentAmmo;
            
            if (weapon.isReloading) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "RELOADING...", 800 - 200, 600 - 20);
            } else {
                font.draw(batch, "WEAPON: " + weapon.type + " [" + ammoText + "]", 
                         800 - 350, 600 - 20);
            }
        }
        batch.end();
    }

    private void renderGameOver() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", 400 - 60, 300 + 20);
        font.setColor(Color.WHITE);
        font.draw(batch, "Press any key to restart", 400 - 110, 300 - 10);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
