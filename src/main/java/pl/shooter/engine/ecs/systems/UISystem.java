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
import pl.shooter.engine.ecs.components.HealthComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.ScoreComponent;
import pl.shooter.engine.ecs.components.WeaponComponent;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;

import java.util.List;

/**
 * Handles the On-Screen Display (HUD) and messages.
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
                Gdx.app.log("UISystem", "Received message: " + event.text);
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

        renderHUD(health, score, weapon);
        
        if (currentMessage != null) {
            renderMessage();
        }

        if (showFps) {
            renderFps();
        }
    }

    private void renderHUD(HealthComponent health, ScoreComponent score, WeaponComponent weapon) {
        float x = 20;
        float y = 600 - 40;
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        if (health != null) {
            float displayHp = Math.max(0, health.hp);
            float healthPercent = Math.max(0, Math.min(1, displayHp / health.maxHp));
            Color healthColor = Color.GREEN;
            if (healthPercent < 0.6f) healthColor = Color.YELLOW;
            if (healthPercent < 0.3f) healthColor = Color.RED;
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x, y, barWidth * healthPercent, barHeight);
        }

        if (weapon != null && weapon.isReloading) {
            float ry = y - 35;
            float reloadPercent = Math.min(1, weapon.reloadTimer / weapon.reloadTime);
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.rect(x - 2, ry - 2, barWidth + 4, 12);
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(x, ry, barWidth, 8);
            shapeRenderer.setColor(Color.CYAN);
            shapeRenderer.rect(x, ry, barWidth * reloadPercent, 8);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        if (health != null) {
            font.setColor(Color.WHITE);
            int displayHp = (int) Math.max(0, health.hp);
            font.draw(batch, "HEALTH: " + displayHp, x, y + barHeight + 15);
        }

        if (score != null) {
            font.setColor(Color.YELLOW);
            font.draw(batch, "SCORE: " + score.score, x, y - 45);
            font.setColor(Color.RED);
            font.draw(batch, "KILLS: " + score.kills, x, y - 65);
            font.setColor(Color.WHITE);
            font.draw(batch, "WAVE: " + score.wave, 400 - 40, 600 - 20);
        }

        if (weapon != null) {
            renderWeaponInfo(weapon);
        }
        batch.end();
    }

    private void renderWeaponInfo(WeaponComponent weapon) {
        String typeName = weapon.type.name().toLowerCase();
        String iconPath = WEAPON_ICONS_BASE + typeName + "/" + typeName + ".png";
        
        Texture icon = assetService.getTexture(iconPath);
        if (icon == null && Gdx.files.internal(iconPath).exists()) {
            assetService.loadTexture(iconPath);
            assetService.finishLoading();
            icon = assetService.getTexture(iconPath);
        }

        float iconX = 800 - 380;
        float iconY = 600 - 55;
        float iconSize = 48;

        if (icon != null) {
            batch.setColor(Color.WHITE);
            batch.draw(icon, iconX, iconY, iconSize, iconSize);
        }

        font.setColor(Color.CYAN);
        String ammoText = weapon.hasInfiniteAmmo ? weapon.magazineAmmo + " / INF" : weapon.magazineAmmo + " / " + weapon.currentAmmo;
        
        if (weapon.isReloading) {
            font.setColor(Color.ORANGE);
            font.draw(batch, "RELOADING...", 800 - 180, 600 - 20);
        } else {
            font.draw(batch, "WEAPON: " + weapon.type + " [" + ammoText + "]", 
                     iconX + iconSize + 10, 600 - 20);
        }
    }

    private void renderMessage() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        float alpha = Math.min(1.0f, messageTimer / 0.5f);
        font.setColor(1, 1, 0, alpha);
        font.getData().setScale(1.5f);
        
        layout.setText(font, currentMessage);
        float x = (800 - layout.width) / 2;
        float y = 450;
        
        font.draw(batch, currentMessage, x, y);
        font.getData().setScale(1.1f);
        batch.end();
    }

    private void renderFps() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.GREEN);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
        batch.end();
    }

    private void renderGameOver() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.setColor(Color.RED);
        font.getData().setScale(2.5f);
        font.draw(batch, "GAME OVER", 400 - 140, 350);
        font.getData().setScale(1.1f);
        font.setColor(Color.WHITE);
        font.draw(batch, "PRESS R OR SPACE TO RESTART", 400 - 160, 280);
        batch.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
