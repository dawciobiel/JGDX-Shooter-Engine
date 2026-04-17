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
 * Handles the On-Screen Display (HUD) and messages.
 * Refactored for better performance and resource management.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final AssetService assetService;
    private final GlyphLayout layout = new GlyphLayout();
    private boolean showFps = false;

    // Message system
    private String currentMessage = null;
    private float messageTimer = 0;

    // Cached weapon info
    private WeaponComponent.Type lastWeaponType = null;
    private Texture cachedWeaponIcon = null;

    private static final String WEAPON_ICONS_BASE = "assets/graphics/textures/weapons/";
    private static final String DEFAULT_ICON = "assets/graphics/textures/weapons/default/icon.png";

    public UISystem(EntityManager entityManager, AssetService assetService) {
        super(entityManager);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.1f);
        this.viewport = new FitViewport(800, 600);
        this.assetService = assetService;
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
        
        if (messageTimer > 0) {
            messageTimer -= deltaTime;
        } else {
            currentMessage = null;
        }

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

        // Render pass 1: Shapes
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderHealthBar(health, weapon);
        shapeRenderer.end();

        // Render pass 2: Text & Icons
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        renderHUDText(health, score, weapon);
        if (currentMessage != null) renderMessage();
        if (showFps) renderFps();
        batch.end();
    }

    private void renderHealthBar(HealthComponent health, WeaponComponent weapon) {
        float x = 20, y = 600 - 40, w = 200, h = 20;
        
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 2, y - 2, w + 4, h + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, w, h);

        if (health != null) {
            float healthPercent = Math.max(0, Math.min(1, health.hp / health.maxHp));
            Color healthColor = Color.GREEN;
            if (healthPercent < 0.6f) healthColor = Color.YELLOW;
            if (healthPercent < 0.3f) healthColor = Color.RED;
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x, y, w * healthPercent, h);
        }

        if (weapon != null && weapon.isReloading) {
            float ry = y - 35;
            float reloadPercent = Math.min(1, weapon.reloadTimer / weapon.reloadTime);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(x - 2, ry - 2, w + 4, 12);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x, ry, w * reloadPercent, 8);
        }
    }

    private void renderHUDText(HealthComponent health, ScoreComponent score, WeaponComponent weapon) {
        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HEALTH: " + (int)Math.max(0, health.hp), 20, 600 - 5);
        }

        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, 20, 515);
            font.setColor(Color.RED);
            font.draw(batch, "KILLS: " + score.kills, 20, 495);
            font.setColor(Color.WHITE);
            font.draw(batch, "WAVE: " + score.wave, 360, 580);
        }

        if (weapon != null) {
            renderWeaponIcon(weapon);
            font.setColor(Color.CYAN);
            String ammo = weapon.hasInfiniteAmmo ? "INF" : weapon.magazineAmmo + "/" + weapon.currentAmmo;
            if (weapon.isReloading) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "RELOADING...", 620, 580);
            } else {
                font.draw(batch, weapon.type + " [" + ammo + "]", 500, 580);
            }
        }
    }

    private void renderWeaponIcon(WeaponComponent weapon) {
        if (lastWeaponType != weapon.type) {
            lastWeaponType = weapon.type;
            String typeName = weapon.type.name().toLowerCase();
            String path = WEAPON_ICONS_BASE + typeName + "/" + typeName + ".png";
            cachedWeaponIcon = assetService.getTexture(path);
            if (cachedWeaponIcon == null) cachedWeaponIcon = assetService.getTexture(DEFAULT_ICON);
        }

        if (cachedWeaponIcon != null) {
            batch.setColor(Color.WHITE);
            batch.draw(cachedWeaponIcon, 420, 600 - 55, 48, 48);
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
