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
 * Creates entities from external JSON definitions with support for dynamic animations and sounds.
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
        componentAliases.put("HealthPickup", HealthPickupComponent.class);
        componentAliases.put("Name", NameComponent.class);
    }

    public Entity loadFromJson(String internalPath, float x, float y) {
        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (!file.exists()) return null;
            
            JsonNode root = objectMapper.readTree(file.read());
            Entity entity = entityManager.createEntity();

            // 1. Components
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

            // 2. Names (Random selection from array)
            JsonNode namesNode = root.get("names");
            if (namesNode != null && namesNode.isArray() && namesNode.size() > 0) {
                int index = random.nextInt(namesNode.size());
                String selectedName = namesNode.get(index).asText();
                entityManager.addComponent(entity, new NameComponent(selectedName));
            }

            // 3. Animations
            JsonNode animNode = root.get("animations");
            if (animNode != null) {
                AnimationConfig animConfig = objectMapper.treeToValue(animNode, AnimationConfig.class);
                setupAnimationsFromConfig(entity, animConfig);
            }

            // 4. Sounds
            JsonNode soundsNode = root.get("sounds");
            if (soundsNode != null && soundsNode.isObject()) {
                SoundComponent soundComp = new SoundComponent();
                Iterator<Map.Entry<String, JsonNode>> soundFields = soundsNode.fields();
                while (soundFields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = soundFields.next();
                    SoundComponent.Action action = SoundComponent.Action.valueOf(entry.getKey());
                    soundComp.addSound(action, entry.getValue().asText());
                }
                entityManager.addComponent(entity, soundComp);
            }

            return entity;
        } catch (Exception e) {
            Gdx.app.error("EntityFactory", "Failed to load entity from " + internalPath, e);
            return null;
        }
    }

    private void setupAnimationsFromConfig(Entity entity, AnimationConfig config) {
        if (assetService == null) return;
        AnimationComponent animComp = new AnimationComponent(config.width, config.height);
        for (Map.Entry<String, AnimationConfig.StateConfig> entry : config.states.entrySet()) {
            try {
                AnimationComponent.State state = AnimationComponent.State.valueOf(entry.getKey());
                AnimationConfig.StateConfig stateConfig = entry.getValue();
                Animation<TextureRegion> anim;
                if ("SHEET".equals(stateConfig.type)) {
                    anim = createAnimationFromSheet(stateConfig.path, stateConfig.rows, stateConfig.cols, stateConfig.frameDuration);
                } else {
                    anim = createAnimationFromFiles(stateConfig.path, stateConfig.count, stateConfig.frameDuration);
                }
                if (anim != null) animComp.addAnimation(state, anim);
            } catch (Exception e) {}
        }
        entityManager.addComponent(entity, animComp);
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

    public void createShellEjection(float x, float y, float angle) {
        Entity shell = entityManager.createEntity();
        entityManager.addComponent(shell, new TransformComponent(x, y, angle));
        
        // Eject shell to the side (relative to firing angle)
        float ejectAngle = angle - 90 + (random.nextFloat() - 0.5f) * 30f;
        float speed = 100f + random.nextFloat() * 50f;
        float vx = (float)Math.cos(Math.toRadians(ejectAngle)) * speed;
        float vy = (float)Math.sin(Math.toRadians(ejectAngle)) * speed;
        
        entityManager.addComponent(shell, new VelocityComponent(vx, vy));
        entityManager.addComponent(shell, new RenderComponent(Color.GOLD, 1.5f, false)); // Small rectangle
        entityManager.addComponent(shell, new ParticleComponent(0.5f, 0.8f)); // Short life
    }

    public Entity createAmmoPickup(float x, float y, int amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new AmmoPickupComponent(amount));
        return pickup;
    }

    public Entity createHealthPickup(float x, float y, float amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.GREEN, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new HealthPickupComponent(amount));
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
