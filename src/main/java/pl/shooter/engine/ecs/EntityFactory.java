package pl.shooter.engine.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.assets.AssetService;
import pl.shooter.engine.ecs.components.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Creates entities from external JSON definitions with support for aliases and animations.
 */
public class EntityFactory {
    private final EntityManager entityManager;
    private final AssetService assetService;
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends Component>> componentAliases = new HashMap<>();
    private final Random random = new Random();

    public EntityFactory(EntityManager entityManager) {
        this(entityManager, null);
    }

    public EntityFactory(EntityManager entityManager, AssetService assetService) {
        this.entityManager = entityManager;
        this.assetService = assetService;
        this.objectMapper = new ObjectMapper();
        registerDefaultAliases();
    }

    private void registerDefaultAliases() {
        componentAliases.put("Transform", TransformComponent.class);
        componentAliases.put("Velocity", VelocityComponent.class);
        componentAliases.put("Render", RenderComponent.class);
        componentAliases.put("Health", HealthComponent.class);
        componentAliases.put("AI", AIComponent.class);
        componentAliases.put("Weapon", WeaponComponent.class);
        componentAliases.put("Player", PlayerComponent.class);
        componentAliases.put("Texture", TextureComponent.class);
        componentAliases.put("Score", ScoreComponent.class);
        componentAliases.put("Particle", ParticleComponent.class);
        componentAliases.put("Projectile", ProjectileComponent.class);
        componentAliases.put("Collider", ColliderComponent.class);
        componentAliases.put("AmmoPickup", AmmoPickupComponent.class);
    }

    public Entity loadFromJson(String internalPath, float x, float y) {
        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (!file.exists()) return null;
            
            JsonNode root = objectMapper.readTree(file.read());
            Entity entity = entityManager.createEntity();
            JsonNode componentsNode = root.get("components");

            if (componentsNode != null && componentsNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = componentsNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String alias = entry.getKey();
                    JsonNode data = entry.getValue();

                    Class<? extends Component> clazz = componentAliases.get(alias);
                    if (clazz == null) continue;

                    Component component = objectMapper.treeToValue(data, clazz);
                    if (component instanceof TransformComponent tc) {
                        tc.x = x;
                        tc.y = y;
                    }
                    entityManager.addComponent(entity, component);
                }
            }

            if (internalPath.contains("zombie")) {
                setupZombieAnimations(entity);
            }

            return entity;
        } catch (Exception e) {
            Gdx.app.error("EntityFactory", "Failed to load entity", e);
            return null;
        }
    }

    private void setupZombieAnimations(Entity entity) {
        if (assetService == null) return;
        AnimationComponent anim = new AnimationComponent(50f, 50f);
        Animation<TextureRegion> idle = createAnimationFromFiles("assets/tds_zombie/skeleton-idle_", 16, 0.05f);
        Animation<TextureRegion> move = createAnimationFromFiles("assets/tds_zombie/skeleton-move_", 16, 0.05f);
        Animation<TextureRegion> attack = createAnimationFromFiles("assets/tds_zombie/skeleton-attack_", 8, 0.08f);

        if (idle != null) anim.addAnimation(AnimationComponent.State.IDLE, idle);
        if (move != null) anim.addAnimation(AnimationComponent.State.WALK, move);
        if (attack != null) anim.addAnimation(AnimationComponent.State.SHOOT, attack);
        entityManager.addComponent(entity, anim);
    }

    public void createExplosion(float x, float y, Color color) {
        for (int i = 0; i < 10; i++) {
            Entity p = entityManager.createEntity();
            entityManager.addComponent(p, new TransformComponent(x, y));
            float vx = (random.nextFloat() - 0.5f) * 200f;
            float vy = (random.nextFloat() - 0.5f) * 200f;
            entityManager.addComponent(p, new VelocityComponent(vx, vy));
            entityManager.addComponent(p, new RenderComponent(new Color(color.r, color.g, color.b, 1.0f), 2f + random.nextFloat() * 4f, true));
            entityManager.addComponent(p, new ParticleComponent(1.5f, 2.0f));
        }
    }

    public Entity createPlayer(float x, float y) {
        Entity player = entityManager.createEntity();
        entityManager.addComponent(player, new PlayerComponent(200f));
        entityManager.addComponent(player, new WeaponComponent(WeaponComponent.Type.SHOTGUN, 0.4f, 600f, 15f, 5, 20, 4, 1.5f));
        entityManager.addComponent(player, new TransformComponent(x, y));
        entityManager.addComponent(player, new VelocityComponent(0, 0));
        entityManager.addComponent(player, new ColliderComponent(15f));
        entityManager.addComponent(player, new HealthComponent(100));
        entityManager.addComponent(player, new ScoreComponent());

        if (assetService != null) {
            AnimationComponent anim = new AnimationComponent(40f, 40f);
            Animation<TextureRegion> walkAnim = createAnimationFromSheet(
                "assets/2dpixx_-_free_2d_topdown_shooter_pack/2DPIXX - Free Topdown Shooter - Soldier - Walk.png", 
                1, 4, 0.1f
            );
            if (walkAnim != null) {
                anim.addAnimation(AnimationComponent.State.WALK, walkAnim);
                anim.addAnimation(AnimationComponent.State.IDLE, walkAnim);
            }
            entityManager.addComponent(player, anim);
        }
        return player;
    }

    public Entity createAmmoPickup(float x, float y, int amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new AmmoPickupComponent(amount));
        return pickup;
    }

    private Animation<TextureRegion> createAnimationFromSheet(String path, int rows, int cols, float frameDuration) {
        Texture texture = assetService.getTexture(path);
        if (texture == null) return null;
        int tileWidth = texture.getWidth() / cols;
        int tileHeight = texture.getHeight() / rows;
        TextureRegion[][] temp = TextureRegion.split(texture, tileWidth, tileHeight);
        TextureRegion[] frames = new TextureRegion[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = temp[i][j];
            }
        }
        return new Animation<>(frameDuration, frames);
    }

    private Animation<TextureRegion> createAnimationFromFiles(String prefix, int count, float frameDuration) {
        TextureRegion[] frames = new TextureRegion[count + 1];
        boolean foundAny = false;
        for (int i = 0; i <= count; i++) {
            Texture tex = assetService.getTexture(prefix + i + ".png");
            if (tex != null) {
                frames[i] = new TextureRegion(tex);
                foundAny = true;
            }
        }
        return foundAny ? new Animation<>(frameDuration, frames) : null;
    }
}
