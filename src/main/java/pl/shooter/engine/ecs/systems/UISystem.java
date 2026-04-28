package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import pl.shooter.engine.Engine;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.models.EngineConfig;
import pl.shooter.engine.config.models.RenderingConfig;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.MessageEvent;

import java.util.List;

/**
 * Handles HUD and Cursor rendering.
 */
public class UISystem extends GameSystem {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final Viewport viewport;
    private final AssetService assetService;
    private final Vector3 mouseBuffer = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();
    
    private RenderingConfig renderingConfig;
    private EngineConfig engineConfig;
    private String lastWeaponPrefabId = null;
    private Texture cachedWeaponIcon = null;

    private String activeMessage = null;
    private float messageTimer = 0;

    public UISystem(EntityManager entityManager, AssetService assetService, SpriteBatch batch, ShapeRenderer shapeRenderer) {
        super(entityManager);
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.font = new BitmapFont();
        this.font.getData().setScale(1.1f);
        this.assetService = assetService;
        this.viewport = new FitViewport(800, 600);
    }

    public void init(Engine engine, EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
        EventBus eventBus = engine.getEventBus();
        if (eventBus != null) {
            eventBus.subscribe(MessageEvent.class, event -> {
                this.activeMessage = event.text;
                this.messageTimer = event.duration;
            });
        }
    }

    // New helper to pass rendering settings
    public void setRenderingConfig(RenderingConfig config) {
        this.renderingConfig = config;
        this.viewport.setWorldSize(config.width, config.height);
        if (config.ui.useCustomCursor) {
            assetService.loadTexture(config.ui.cursorImagePath);
        }
        if (config.ui.defaultWeaponIconPath != null) {
            assetService.loadTexture(config.ui.defaultWeaponIconPath);
        }
    }

    @Override
    public void update(float deltaTime) {
        viewport.apply();

        if (messageTimer > 0) {
            messageTimer -= deltaTime;
            if (messageTimer <= 0) activeMessage = null;
        }

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class);
        if (players.isEmpty()) return;

        Entity player = players.getFirst();
        HealthComponent health = entityManager.getComponent(player, HealthComponent.class);
        ScoreComponent score = entityManager.getComponent(player, ScoreComponent.class);
        InventoryComponent inv = entityManager.getComponent(player, InventoryComponent.class);
        WeaponComponent weapon = entityManager.getComponent(player, WeaponComponent.class);

        // Update Weapon Icon Cache
        if (weapon != null && (lastWeaponPrefabId == null || !lastWeaponPrefabId.equals(weapon.name))) {
            lastWeaponPrefabId = weapon.name;
            if (weapon.iconPath != null) {
                assetService.loadTexture(weapon.iconPath);
                cachedWeaponIcon = assetService.getTexture(weapon.iconPath);
            }
            if (cachedWeaponIcon == null && renderingConfig != null) {
                cachedWeaponIcon = assetService.getTexture(renderingConfig.ui.defaultWeaponIconPath);
            }
        }

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderHUDBackgrounds(health);
        shapeRenderer.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        renderHUDText(health, score, weapon, inv);
        
        // RENDER CUSTOM CURSOR
        if (renderingConfig != null && renderingConfig.ui.useCustomCursor) {
            renderCursor();
        }

        // RENDER DEBUG INFO (FPS)
        if (engineConfig != null && engineConfig.debug.showFps) {
            renderDebugInfo();
        }
        
        batch.end();
    }

    private void renderDebugInfo() {
        font.setColor(Color.YELLOW);
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, viewport.getWorldHeight() - 10);
        font.setColor(Color.WHITE);
    }

    private void renderCursor() {
        Texture cursorTex = assetService.getTexture(renderingConfig.ui.cursorImagePath);
        if (cursorTex != null) {
            // Get raw screen coords and map to UI viewport
            float mx = Gdx.input.getX();
            float my = Gdx.input.getY();
            mouseBuffer.set(mx, my, 0);
            viewport.unproject(mouseBuffer);
            
            float size = 32f;
            batch.draw(cursorTex, mouseBuffer.x - size/2, mouseBuffer.y - size/2, size, size);
        }
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
    }

    private void renderHUDText(HealthComponent health, ScoreComponent score, WeaponComponent weapon, InventoryComponent inv) {
        float vh = viewport.getWorldHeight();
        float vw = viewport.getWorldWidth();

        // 1. HP
        if (health != null) {
            font.setColor(Color.WHITE);
            font.draw(batch, "HP: " + (int)health.hp, 25, vh - 23);
        }

        // 2. Score & Kills (Top Right)
        if (score != null) {
            font.setColor(Color.GOLD);
            font.draw(batch, "SCORE: " + score.score, vw - 180, vh - 20);
            font.setColor(Color.WHITE);
            font.draw(batch, "KILLS: " + score.kills, vw - 180, vh - 45);
            font.setColor(Color.RED);
            font.draw(batch, "WAVE: " + score.wave, vw - 180, vh - 70);
        }

        // 3. Weapon & Ammo (Bottom Right)
        if (weapon != null) {
            font.setColor(Color.CYAN);
            font.draw(batch, weapon.name, vw - 180, 80);
            
            String ammoText;
            if (weapon.magazineSize > 0) {
                int reserve = (inv != null && weapon.activeAmmo != null) ? inv.getAmmoCount(weapon.activeAmmo.id) : 0;
                ammoText = weapon.magazineAmmo + " / " + reserve;
            } else {
                ammoText = "---";
            }
            font.setColor(Color.WHITE);
            font.draw(batch, "AMMO: " + ammoText, vw - 180, 55);

            if (cachedWeaponIcon != null) {
                batch.draw(cachedWeaponIcon, vw - 240, 45, 48, 48);
            }
        }

        // 4. Center Messages (Multi-kills, etc.)
        if (activeMessage != null) {
            layout.setText(font, activeMessage);
            font.setColor(Color.YELLOW);
            font.draw(batch, activeMessage, (vw - layout.width) / 2f, vh * 0.75f);
            font.setColor(Color.WHITE);
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void dispose() { font.dispose(); }
}
