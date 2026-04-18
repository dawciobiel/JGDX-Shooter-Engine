package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;

import java.util.List;

/**
 * Handles the On-Screen Display (HUD).
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final AssetService assetService;
    private final GlyphLayout layout = new GlyphLayout();
    private boolean showFps = false;

    private String currentMessage = null;
    private float messageTimer = 0;

    private WeaponComponent.Type lastWeaponType = null;
    private Texture cachedWeaponIcon = null;

    private static final String DEFAULT_ICON = "weapons/default/icon.png";

    public UISystem(EntityManager entityManager, AssetService assetService) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.1f);
        this.viewport = new FitViewport(800, 600);
        this.assetService = assetService;
        
        this.assetService.loadTexture(DEFAULT_ICON);
    }

    public void init(EventBus eventBus) {
        if (eventBus != null) {
            eventBus.subscribe(MessageEvent.class, event -> {
                this.currentMessage = event.text;
                this.messageTimer = event.duration;
            });
        }
    }

    public void setShowFps(boolean show) { this.showFps = show; }

    @Override
    public void update(float deltaTime) {
        viewport.apply();
        if (messageTimer > 0) messageTimer -= deltaTime;
        else currentMessage = null;

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class);
        if (players.isEmpty()) {
            renderGameOver();
            return;
        }

        Entity player = players.get(0);
        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
        if (health != null && health.isDead) {
            renderGameOver();
            return;
        }

        ScoreComponent score = entityManager.getComponent(player, ScoreComponent.class);
        WeaponComponent weapon = entityManager.getComponent(player, WeaponComponent.class);

        // BACKGROUNDS
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderHUDBackgrounds(health, weapon);
        shapeRenderer.end();

        // ICONS & TEXT
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        renderHUDText(health, score, weapon);
        if (currentMessage != null) renderMessage();
        if (showFps) renderFps();
        batch.end();
    }

    private void renderHUDBackgrounds(HealthComponent health, WeaponComponent weapon) {
        float hx = 20, hy = 560, hw = 200, hh = 20;
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(hx - 2, hy - 2, hw + 4, hh + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(hx, hy, hw, hh);

        if (health != null) {
            float healthPercent = Math.max(0, Math.min(1, health.hp / health.maxHp));
            shapeRenderer.setColor(healthPercent < 0.3f ? Color.RED : (healthPercent < 0.6f ? Color.YELLOW : Color.GREEN));
            shapeRenderer.rect(hx, hy, hw * healthPercent, hh);
        }

        // Weapon Slot
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(800 - 84, 16, 68, 68);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(800 - 80, 20, 60, 60);
    }

    private void renderHUDText(HealthComponent health, ScoreComponent score, WeaponComponent weapon) {
        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HP: " + (int)Math.max(0, health.hp), 25, 577);
        }
        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, 350, 580);
            font.setColor(Color.WHITE);
            font.draw(batch, "WAVE: " + score.wave, 365, 555);
        }
        if (weapon != null) {
            renderWeaponIcon(weapon);
            font.setColor(Color.CYAN);
            String ammo = weapon.hasInfiniteAmmo ? "INF" : weapon.magazineAmmo + "/" + weapon.currentAmmo;
            layout.setText(font, weapon.type.name());
            font.draw(batch, weapon.type.name(), 800 - layout.width - 95, 65);
            font.draw(batch, "[" + ammo + "]", 800 - 155, 40);
            
            if (weapon.isReloading) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "RELOADING...", 800 - 200, 90);
            }
        }
    }

    private void renderWeaponIcon(WeaponComponent weapon) {
        if (lastWeaponType != weapon.type) {
            lastWeaponType = weapon.type;
            String typeName = weapon.type.name().toLowerCase();
            String iconName = "weapons/" + typeName + "/" + typeName + ".png";
            cachedWeaponIcon = assetService.getTexture(iconName);
            if (cachedWeaponIcon == null) {
                cachedWeaponIcon = assetService.getTexture(DEFAULT_ICON);
            }
        }
        if (cachedWeaponIcon != null) {
            batch.draw(cachedWeaponIcon, 800 - 75, 25, 50, 50);
        }
    }

    private void renderMessage() {
        float alpha = Math.min(1.0f, messageTimer / 0.5f);
        font.setColor(1, 1, 0, alpha);
        font.getData().setScale(1.5f);
        layout.setText(font, currentMessage);
        font.draw(batch, currentMessage, (800 - layout.width) / 2, 450);
        font.getData().setScale(1.1f);
    }

    private void renderFps() {
        font.setColor(Color.GREEN);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
    }

    private void renderGameOver() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.RED);
        font.getData().setScale(2.5f);
        font.draw(batch, "GAME OVER", 260, 350);
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        font.draw(batch, "PRESS R OR SPACE TO RESTART", 240, 280);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
