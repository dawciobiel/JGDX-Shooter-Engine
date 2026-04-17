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
 * DEBUG VERSION: Enforces specific colors for groups to trace lighting glitches.
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
        componentAliases.put("Trigger", TriggerComponent.class);
        componentAliases.put("Pushable", PushableComponent.class);
        componentAliases.put("Obstacle", ObstacleComponent.class);
        componentAliases.put("Destructible", DestructibleComponent.class);
        componentAliases.put("Interactable", InteractableComponent.class);
        componentAliases.put("Door", DoorComponent.class);
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
                    Class<? extends Component> clazz = componentAliases.get(alias);
                    if (clazz == null) continue;

                    Component component = objectMapper.treeToValue(entry.getValue(), clazz);
                    if (component instanceof TransformComponent tc) {
                        tc.x = x;
                        tc.y = y;
                    }
                    if (component instanceof TextureComponent tex && assetService != null) {
                        assetService.loadTexture(tex.assetPath);
                    }
                    entityManager.addComponent(entity, component);
                }
            }

            // 2. Names
            JsonNode namesNode = root.get("names");
            if (namesNode != null && namesNode.isArray() && namesNode.size() > 0) {
                int index = random.nextInt(namesNode.size());
                entityManager.addComponent(entity, new NameComponent(namesNode.get(index).asText()));
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
                    try {
                        SoundComponent.Action action = SoundComponent.Action.valueOf(entry.getKey());
                        soundComp.addSound(action, entry.getValue().asText());
                    } catch (Exception e) {}
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
            // DEBUG: Explosion color orange-ish
            entityManager.addComponent(p, new RenderComponent(new Color(1, 0.5f, 0, 1.0f), 2f + random.nextFloat() * 4f, true));
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

    public Entity createAmmoPickup(float x, float y, int amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        // DEBUG: BLUE for pickups
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new AmmoPickupComponent(amount));
        return pickup;
    }

    public Entity createHealthPickup(float x, float y, float amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        // DEBUG: BLUE for pickups
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new HealthPickupComponent(amount));
        return pickup;
    }

    public Entity createTrigger(float x, float y, float radius, TriggerComponent.TriggerType type, String value) {
        Entity trigger = entityManager.createEntity();
        entityManager.addComponent(trigger, new TransformComponent(x, y));
        entityManager.addComponent(trigger, new ColliderComponent(radius));
        entityManager.addComponent(trigger, new TriggerComponent(type, value));
        return trigger;
    }

    private Animation<TextureRegion> createAnimationFromSheet(String path, int rows, int cols, float frameDuration) {
        if (assetService == null) return null;
        Texture texture = assetService.getTexture(path);
        if (texture == null) { assetService.loadTexture(path); return null; }
        TextureRegion[][] temp = TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight() / rows);
        TextureRegion[] frames = new TextureRegion[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) frames[index++] = temp[i][j];
        return new Animation<>(frameDuration, frames);
    }

    private Animation<TextureRegion> createAnimationFromFiles(String prefix, int count, float frameDuration) {
        if (assetService == null) return null;
        TextureRegion[] frames = new TextureRegion[count + 1];
        boolean foundAny = false;
        for (int i = 0; i <= count; i++) {
            String path = prefix + i + ".png";
            Texture tex = assetService.getTexture(path);
            if (tex == null) { assetService.loadTexture(path); continue; }
            frames[i] = new TextureRegion(tex); foundAny = true;
        }
        return foundAny ? new Animation<>(frameDuration, frames) : null;
    }
}
