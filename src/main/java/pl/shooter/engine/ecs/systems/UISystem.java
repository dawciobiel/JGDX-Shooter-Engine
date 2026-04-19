package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;

import java.util.List;
import java.util.Map;

/**
 * Handles the On-Screen Display (HUD) and Profiler.
 * Optimized to use shared SpriteBatch and ShapeRenderer.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final AssetService assetService;
    private final GameConfig config;
    private final GlyphLayout layout = new GlyphLayout();
    private Engine engine;
    private boolean showFps = false;

    private String currentMessage = null;
    private float messageTimer = 0;

    private WeaponComponent.Type lastWeaponType = null;
    private Texture cachedWeaponIcon = null;

    private static final String DEFAULT_ICON = "weapons/default/icon.png";

    public UISystem(EntityManager entityManager, AssetService assetService, SpriteBatch batch, ShapeRenderer shapeRenderer) {
        super(entityManager);
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.1f);
        this.assetService = assetService;
        this.config = assetService.getConfig();
        this.viewport = new FitViewport(config.graphics.width, config.graphics.height);
        
        this.assetService.loadTexture(DEFAULT_ICON);
    }

    public void init(Engine engine) {
        this.engine = engine;
        EventBus eventBus = engine.getEventBus();
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

        Entity player = players.getFirst();
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
        renderHUDBackgrounds(health);
        if (config.debug.showProfiler) renderProfilerBackground();
        shapeRenderer.end();

        // ICONS & TEXT
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        renderHUDText(health, score, weapon);
        if (currentMessage != null) renderMessage();
        if (showFps) renderFps();
        if (config.debug.showProfiler) renderProfilerText();
        batch.end();
    }

    private void renderHUDBackgrounds(HealthComponent health) {
        float hx = 20, hy = viewport.getWorldHeight() - 40, hw = 200, hh = 20;
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(hx - 2, hy - 2, hw + 4, hh + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(hx, hy, hw, hh);

        if (health != null) {
            float healthPercent = MathUtils.clamp(health.hp / health.maxHp, 0, 1);
            shapeRenderer.setColor(healthPercent < 0.3f ? Color.RED : (healthPercent < 0.6f ? Color.YELLOW : Color.GREEN));
            shapeRenderer.rect(hx, hy, hw * healthPercent, hh);
        }

        // Weapon Slot
        float ww = viewport.getWorldWidth();
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(ww - 84, 16, 68, 68);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(ww - 80, 20, 60, 60);
    }

    private void renderHUDText(HealthComponent health, ScoreComponent score, WeaponComponent weapon) {
        float vh = viewport.getWorldHeight();
        float vw = viewport.getWorldWidth();

        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HP: " + (int)Math.max(0, health.hp), 25, vh - 23);
        }
        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, vw / 2 - 50, vh - 20);
            font.setColor(Color.WHITE);
            font.draw(batch, "WAVE: " + score.wave, vw / 2 - 35, vh - 45);
        }
        if (weapon != null) {
            renderWeaponIcon(weapon);
            font.setColor(Color.CYAN);
            String ammo = weapon.hasInfiniteAmmo ? "INF" : weapon.magazineAmmo + "/" + weapon.currentAmmo;
            layout.setText(font, weapon.type.name());
            font.draw(batch, weapon.type.name(), vw - layout.width - 95, 65);
            font.draw(batch, "[" + ammo + "]", vw - 155, 40);
            
            if (weapon.isReloading) {
                font.setColor(Color.ORANGE);
                font.draw(batch, "RELOADING...", vw - 200, 90);
            }
        }
    }

    private void renderProfilerBackground() {
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(10, 50, 250, 400);
    }

    private void renderProfilerText() {
        if (engine == null) return;
        Map<String, Long> data = engine.getPerformanceData();
        float y = 440;
        font.getData().setScale(0.8f);
        font.setColor(Color.GOLD);
        font.draw(batch, "SYSTEM PERFORMANCE (ms)", 20, y);
        y -= 20;

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            double ms = entry.getValue() / 1_000_000.0;
            font.setColor(ms > 1.0 ? Color.RED : (ms > 0.5 ? Color.YELLOW : Color.WHITE));
            font.draw(batch, String.format("%s: %.3f", entry.getKey(), ms), 20, y);
            y -= 15;
        }
        font.getData().setScale(1.1f);
    }

    private void renderWeaponIcon(WeaponComponent weapon) {
        float vw = viewport.getWorldWidth();
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
            batch.draw(cachedWeaponIcon, vw - 75, 25, 50, 50);
        }
    }

    private void renderMessage() {
        float alpha = Math.min(1.0f, messageTimer / 0.5f);
        font.setColor(1, 1, 0, alpha);
        font.getData().setScale(1.5f);
        layout.setText(font, currentMessage);
        font.draw(batch, currentMessage, (viewport.getWorldWidth() - layout.width) / 2, 450);
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
        font.draw(batch, "GAME OVER", viewport.getWorldWidth() / 2 - 140, viewport.getWorldHeight() / 2 + 50);
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        font.draw(batch, "PRESS R OR SPACE TO RESTART", viewport.getWorldWidth() / 2 - 160, viewport.getWorldHeight() / 2 - 20);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void dispose() {
        font.dispose();
    }
}
