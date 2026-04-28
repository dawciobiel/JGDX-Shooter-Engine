package pl.shooter.engine.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.models.*;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.graphics.CharacterRendererFactory;

import java.util.Map;
import java.util.Random;

/**
 * Strict Entity Factory.
 * Assembles entities based on provided prefabs.
 */
public class EntityFactory {
    private final EntityManager entityManager;
    private final AssetService assetService;
    private final ConfigService configService;
    private final Random random = new Random();

    public EntityFactory(EntityManager entityManager, AssetService assetService, ConfigService configService) {
        this.entityManager = entityManager;
        this.assetService = assetService;
        this.configService = configService;
    }

    public void setCurrentMapFolder(String folder) {}

    // --- CHARACTER CREATION ---

    public Entity createPlayer(float x, float y, PlayerConfig playerConfig, MapConfig.LevelSettings.StartingEquipment equipment) {
        CharacterPrefab character = configService.loadPrefab(playerConfig.characterPrefab, CharacterPrefab.class);
        if (character == null) return null;

        Entity entity = entityManager.createEntity();
        entityManager.addComponent(entity, new TransformComponent(x, y));
        
        PlayerComponent pc = new PlayerComponent(character.stats.speed);
        pc.invincible = playerConfig.invincible;
        pc.infiniteAmmo = playerConfig.infiniteAmmo;
        entityManager.addComponent(entity, pc);
        
        entityManager.addComponent(entity, new HealthComponent(character.stats));
        
        VelocityComponent vc = new VelocityComponent(0, 0);
        vc.baseSpeed = character.stats.speed;
        entityManager.addComponent(entity, vc);
        
        entityManager.addComponent(entity, new ColliderComponent(character.stats));
        entityManager.addComponent(entity, new TextureComponent(character));
        entityManager.addComponent(entity, new CharacterRendererComponent(CharacterRendererFactory.create(character)));
        
        // Setup Inventory with starting equipment from MapConfig
        InventoryComponent inv = new InventoryComponent();
        if (equipment != null) {
            if (equipment.weapons != null) {
                for (String weaponPath : equipment.weapons) {
                    WeaponPrefab wp = configService.loadPrefab(weaponPath, WeaponPrefab.class);
                    if (wp != null) {
                        WeaponComponent wc = new WeaponComponent(wp);
                        if (wc.allowedAmmoCategories != null && !wc.allowedAmmoCategories.isEmpty()) {
                            for (String ammoId : equipment.ammo.keySet()) {
                                AmmoPrefab ap = configService.loadPrefab("ammo/" + ammoId, AmmoPrefab.class);
                                if (ap != null && wc.allowedAmmoCategories.contains(ap.category)) {
                                    wc.activeAmmo = ap;
                                    break;
                                }
                            }
                        }
                        inv.addWeapon(wc);
                    }
                }
            }
            if (equipment.ammo != null) {
                for (Map.Entry<String, Integer> ammo : equipment.ammo.entrySet()) {
                    inv.addAmmo(ammo.getKey(), ammo.getValue());
                }
            }
        }
        entityManager.addComponent(entity, inv);
        
        // Make sure first weapon is active on entity
        if (inv.getActiveWeapon() != null) {
            entityManager.addComponent(entity, inv.getActiveWeapon());
        }

        entityManager.addComponent(entity, new ScoreComponent());
        entityManager.addComponent(entity, new NameComponent(playerConfig.nickname));

        setupAnimations(entity, character);
        return entity;
    }

    public Entity createEnemy(String prefabPath, float x, float y) {
        CharacterPrefab character = configService.loadPrefab(prefabPath, CharacterPrefab.class);
        if (character == null) return null;

        Entity entity = entityManager.createEntity();
        TransformComponent tc = new TransformComponent(x, y);
        entityManager.addComponent(entity, tc);
        
        AIComponent ai = new AIComponent();
        ai.behavior = AIComponent.Behavior.CHASE; // Enable pathfinding by default
        ai.attackRange = character.melee.range;
        ai.attackDamage = character.melee.damage;
        ai.attackRate = character.melee.attackRate;
        entityManager.addComponent(entity, ai);
        
        VelocityComponent vc = new VelocityComponent(0, 0);
        vc.baseSpeed = character.stats.speed;
        entityManager.addComponent(entity, vc);
        
        entityManager.addComponent(entity, new SteeringComponent(tc, vc));
        entityManager.addComponent(entity, new HealthComponent(character.stats));
        entityManager.addComponent(entity, new ColliderComponent(character.stats));
        entityManager.addComponent(entity, new TextureComponent(character));
        entityManager.addComponent(entity, new CharacterRendererComponent(CharacterRendererFactory.create(character)));

        setupAnimations(entity, character);
        return entity;
    }

    private void setupAnimations(Entity entity, CharacterPrefab prefab) {
        float width = prefab.visuals.frameWidth > 0 ? prefab.visuals.frameWidth : 32f;
        float height = prefab.visuals.frameHeight > 0 ? prefab.visuals.frameHeight : 32f;
        AnimationComponent animComp = new AnimationComponent(width, height);
        
        if (prefab.visuals.animations != null && !prefab.visuals.animations.isEmpty()) {
            for (Map.Entry<String, CharacterPrefab.AnimationData> entry : prefab.visuals.animations.entrySet()) {
                AnimationComponent.State state;
                try { state = AnimationComponent.State.valueOf(entry.getKey().toUpperCase()); } catch (Exception e) { continue; }
                CharacterPrefab.AnimationData data = entry.getValue();
                Animation<TextureRegion> anim;
                if ("SHEET".equals(data.type)) {
                    String resolved = assetService.resolvePath(data.path, "textures");
                    anim = (resolved != null) ? createAnimationFromSheet(resolved, data.rows, data.cols, data.frameDuration) : null;
                } else if ("ATLAS_REGION".equals(data.type)) {
                    anim = createAnimationFromAtlasRegion(
                            data.path,
                            data.region,
                            data.frameDuration,
                            data.rows,
                            data.cols,
                            data.count
                    );
                } else {
                    anim = createAnimationFromFiles(data.path, data.count, data.frameDuration);
                }
                if (anim != null) animComp.addAnimation(state, anim);
            }
        }
        entityManager.addComponent(entity, animComp);
    }

    private Animation<TextureRegion> createAnimationFromSheet(String path, int rows, int cols, float frameDuration) {
        Texture texture = assetService.getTexture(path);
        if (texture == null) { assetService.loadTexture(path); return null; }
        TextureRegion[][] temp = TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) frames[index++] = temp[i][j];
        return new Animation<>(frameDuration, frames);
    }

    private Animation<TextureRegion> createAnimationFromFiles(String pathPrefix, int count, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[count];
        boolean foundAny = false;
        for (int i = 0; i < count; i++) {
            String frameFileName = pathPrefix + "_" + i + ".png";
            String resolved = assetService.resolvePath(frameFileName, "textures");
            if (resolved != null) {
                Texture tex = assetService.getTexture(resolved);
                if (tex == null) { assetService.loadTexture(resolved); }
                else { frames[i] = new TextureRegion(tex); foundAny = true; }
            }
        }
        return foundAny ? new Animation<>(frameDuration, frames) : null;
    }

    private Animation<TextureRegion> createAnimationFromAtlasRegion(String atlasPath, String regionName, float frameDuration, int rows, int cols, int count) {
        TextureAtlas atlas = assetService.getAtlas(atlasPath);
        if (atlas == null) {
            assetService.loadAtlas(atlasPath);
            return null;
        }

        TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
        if (region == null) {
            Gdx.app.error("EntityFactory", "Missing atlas region '" + regionName + "' in " + atlasPath);
            return null;
        }

        if (rows <= 1 && cols <= 1) {
            return new Animation<>(frameDuration, region);
        }

        int frameWidth = region.getRegionWidth() / cols;
        int frameHeight = region.getRegionHeight() / rows;
        int frameCount = count > 0 ? Math.min(count, rows * cols) : rows * cols;
        TextureRegion[] frames = new TextureRegion[frameCount];
        int index = 0;

        for (int row = 0; row < rows && index < frameCount; row++) {
            for (int col = 0; col < cols && index < frameCount; col++) {
                frames[index++] = new TextureRegion(
                        region.getTexture(),
                        region.getRegionX() + col * frameWidth,
                        region.getRegionY() + row * frameHeight,
                        frameWidth,
                        frameHeight
                );
            }
        }

        return new Animation<>(frameDuration, frames);
    }

    public void createExplosion(float x, float y, Color color) {
        for (int i = 0; i < 10; i++) {
            Entity p = entityManager.createEntity();
            entityManager.addComponent(p, new TransformComponent(x, y));
            float vx = (random.nextFloat() - 0.5f) * 200f, vy = (random.nextFloat() - 0.5f) * 200f;
            entityManager.addComponent(p, new VelocityComponent(vx, vy));
            entityManager.addComponent(p, new RenderComponent(color != null ? color : new Color(1, 0.5f, 0, 1.0f), 2f + random.nextFloat() * 4f, true));
            entityManager.addComponent(p, new ParticleComponent(1.5f, 2.0f));
        }
    }

    public void createShellEjection(float x, float y, float angle) {
        Entity shell = entityManager.createEntity();
        entityManager.addComponent(shell, new TransformComponent(x, y, angle));
        float ejectAngle = angle - 90 + (random.nextFloat() - 0.5f) * 30f;
        float speed = 100f + random.nextFloat() * 50f;
        entityManager.addComponent(shell, new VelocityComponent((float)Math.cos(Math.toRadians(ejectAngle)) * speed, (float)Math.sin(Math.toRadians(ejectAngle)) * speed));
        entityManager.addComponent(shell, new RenderComponent(Color.GOLD, 1.5f, false));
        entityManager.addComponent(shell, new ParticleComponent(0.5f, 0.8f));
    }

    public void createHealthPickup(float x, float y, float amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new HealthPickupComponent(amount));
    }

    public void createAmmoBox(String ammoPrefabPath, int quantity, float x, float y) {
        AmmoPrefab ammo = configService.loadPrefab(ammoPrefabPath, AmmoPrefab.class);
        if (ammo == null) return;
        Entity entity = entityManager.createEntity();
        entityManager.addComponent(entity, new TransformComponent(x, y));
        entityManager.addComponent(entity, new AmmoPickupComponent(ammo.id, quantity));
        entityManager.addComponent(entity, new ColliderComponent(12f));
        entityManager.addComponent(entity, new TextureComponent(ammo.iconPath, 24, 24));
    }

    public Entity createObject(String prefabPath, float x, float y, boolean pushable, boolean destructible) {
        Entity entity = entityManager.createEntity();
        entityManager.addComponent(entity, new TransformComponent(x, y));
        CharacterPrefab visualData = configService.loadPrefab(prefabPath, CharacterPrefab.class);
        if (visualData != null) {
            entityManager.addComponent(entity, new TextureComponent(visualData));
            entityManager.addComponent(entity, new ColliderComponent(visualData.stats));
            entityManager.addComponent(entity, new CharacterRendererComponent(CharacterRendererFactory.create(visualData)));
            if (destructible) entityManager.addComponent(entity, new HealthComponent(visualData.stats));
        }
        if (pushable) entityManager.addComponent(entity, new PushableComponent());
        return entity;
    }

    public Entity createNeutral(String prefabPath, float x, float y) {
        CharacterPrefab character = configService.loadPrefab(prefabPath, CharacterPrefab.class);
        if (character == null) return null;
        Entity entity = entityManager.createEntity();
        entityManager.addComponent(entity, new TransformComponent(x, y));
        entityManager.addComponent(entity, new HealthComponent(character.stats));
        entityManager.addComponent(entity, new ColliderComponent(character.stats));
        entityManager.addComponent(entity, new TextureComponent(character));
        entityManager.addComponent(entity, new CharacterRendererComponent(CharacterRendererFactory.create(character)));
        setupAnimations(entity, character);
        return entity;
    }

    public Entity createProjectile(String prefabPath, Entity owner, float x, float y, float angle) {
        ProjectilePrefab prefab = configService.loadPrefab(prefabPath, ProjectilePrefab.class);
        if (prefab == null) return null;
        Entity projectile = entityManager.createEntity();
        entityManager.addComponent(projectile, new TransformComponent(x, y, angle));
        float vx = (float) Math.cos(Math.toRadians(angle)) * prefab.speed;
        float vy = (float) Math.sin(Math.toRadians(angle)) * prefab.speed;
        entityManager.addComponent(projectile, new VelocityComponent(vx, vy));
        entityManager.addComponent(projectile, new ProjectileComponent(1.5f, owner.getId(), (int)prefab.baseDamage));
        entityManager.addComponent(projectile, new ColliderComponent(prefab));
        entityManager.addComponent(projectile, new TextureComponent(prefab));
        return projectile;
    }
}
