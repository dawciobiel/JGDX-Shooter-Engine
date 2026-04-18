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
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.config.JsonService;
import pl.shooter.engine.ecs.components.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Creates entities using shared ObjectMapper from JsonService.
 * Uses GameConfig for path resolution.
 */
public class EntityFactory {
    private final EntityManager entityManager;
    private final AssetService assetService;
    private final GameConfig config;
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends Component>> componentAliases = new HashMap<>();
    private final Random random = new Random();
    private String currentMapFolder = null;

    public EntityFactory(EntityManager entityManager, AssetService assetService, GameConfig config) {
        this.entityManager = entityManager;
        this.assetService = assetService;
        this.config = config != null ? config : new GameConfig();
        this.objectMapper = JsonService.getMapper(); // Use Singleton
        registerDefaultAliases();
    }

    public void setCurrentMapFolder(String folder) { this.currentMapFolder = folder; }

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
        componentAliases.put("Inventory", InventoryComponent.class);
    }

    public Entity loadEntity(String type, float x, float y) {
        String path = resolveEntityPath(type);
        if (path == null) {
            Gdx.app.error("EntityFactory", "COULD NOT RESOLVE ENTITY PATH for: " + type);
            return null;
        }
        Entity entity = loadFromJson(path, x, y);
        if (entity != null && x <= -999) {
            entityManager.removeEntity(entity);
        }
        return entity;
    }

    private String resolveEntityPath(String type) {
        String[] possibleSubfolders = {
            config.paths.enemies + "/",
            config.paths.triggers + "/",
            config.paths.objects + "/",
            config.paths.player + "/",
            config.paths.entities + "/"
        };

        // 1. TRY CORE ASSETS FIRST (As requested)
        for (String sub : possibleSubfolders) {
            String p = config.paths.coreAssets + "/" + sub + type + ".json";
            if (Gdx.files.internal(p).exists()) return p;
        }

        // 2. TRY MAP FOLDER
        if (currentMapFolder != null) {
            for (String sub : possibleSubfolders) {
                String p = currentMapFolder + "/" + sub + type + ".json";
                if (Gdx.files.internal(p).exists()) return p;
            }
        }

        // 3. ABSOLUTE FALLBACK
        String fallback = config.paths.maps + "/default/" + config.paths.entities + "/" + type + ".json";
        if (Gdx.files.internal(fallback).exists()) return fallback;

        return null;
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
                    Class<? extends Component> clazz = componentAliases.get(entry.getKey());
                    if (clazz == null) continue;

                    Component component = objectMapper.treeToValue(entry.getValue(), clazz);
                    if (component instanceof TransformComponent tc) {
                        tc.x = x; tc.y = y;
                    }
                    if (component instanceof TextureComponent tex && assetService != null) {
                        tex.assetPath = assetService.resolvePath(tex.assetPath, config.paths.textures);
                        assetService.loadTexture(tex.assetPath);
                    }
                    entityManager.addComponent(entity, component);
                }
            }

            if (root.has("names")) {
                JsonNode namesNode = root.get("names");
                entityManager.addComponent(entity, new NameComponent(namesNode.get(random.nextInt(namesNode.size())).asText()));
            }

            if (root.has("animations")) {
                AnimationConfig animConfig = objectMapper.treeToValue(root.get("animations"), AnimationConfig.class);
                setupAnimationsFromConfig(entity, animConfig);
            }

            if (root.has("sounds") && assetService != null) {
                SoundComponent soundComp = new SoundComponent();
                Iterator<Map.Entry<String, JsonNode>> soundFields = root.get("sounds").fields();
                while (soundFields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = soundFields.next();
                    try {
                        SoundComponent.Action action = SoundComponent.Action.valueOf(entry.getKey());
                        soundComp.addSound(action, assetService.resolvePath(entry.getValue().asText(), config.paths.sounds));
                    } catch (Exception e) {
                        Gdx.app.error("EntityFactory", "Error parsing sound action: " + entry.getKey());
                    }
                }
                entityManager.addComponent(entity, soundComp);
            }

            return entity;
        } catch (Exception e) {
            Gdx.app.error("EntityFactory", "Failed to load entity from JSON: " + internalPath, e);
            return null;
        }
    }

    private void setupAnimationsFromConfig(Entity entity, AnimationConfig config) {
        if (assetService == null) return;
        AnimationComponent animComp = new AnimationComponent(config.width, config.height);
        for (Map.Entry<String, AnimationConfig.StateConfig> entry : config.states.entrySet()) {
            try {
                AnimationComponent.State state = AnimationComponent.State.valueOf(entry.getKey());
                AnimationConfig.StateConfig sc = entry.getValue();
                String resolvedPath = assetService.resolvePath(sc.path, this.config.paths.textures);
                Animation<TextureRegion> anim;
                if ("SHEET".equals(sc.type)) {
                    anim = createAnimationFromSheet(resolvedPath, sc.rows, sc.cols, sc.frameDuration);
                } else {
                    anim = createAnimationFromFiles(resolvedPath, sc.count, sc.frameDuration);
                }
                if (anim != null) animComp.addAnimation(state, anim);
            } catch (Exception e) {
                Gdx.app.error("EntityFactory", "Error setting up animation: " + entry.getKey());
            }
        }
        entityManager.addComponent(entity, animComp);
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

    public void createAmmoPickup(float x, float y, int amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new AmmoPickupComponent(amount));
    }

    public void createHealthPickup(float x, float y, float amount) {
        Entity pickup = entityManager.createEntity();
        entityManager.addComponent(pickup, new TransformComponent(x, y));
        entityManager.addComponent(pickup, new RenderComponent(Color.BLUE, 8f, true));
        entityManager.addComponent(pickup, new ColliderComponent(8f));
        entityManager.addComponent(pickup, new HealthPickupComponent(amount));
    }
}
