package pl.shooter.engine.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.shooter.engine.ecs.components.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Creates entities from external JSON definitions with support for aliases.
 */
public class EntityFactory {
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final Map<String, Class<? extends Component>> componentAliases = new HashMap<>();
    private final Random random = new Random();

    public EntityFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
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
    }

    public Entity loadFromJson(String internalPath, float x, float y) {
        try {
            FileHandle file = Gdx.files.internal(internalPath);
            if (!file.exists()) {
                Gdx.app.error("EntityFactory", "File not found: " + internalPath);
                return null;
            }
            
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
                    if (clazz == null) {
                        Gdx.app.error("EntityFactory", "Unknown component alias: " + alias);
                        continue;
                    }

                    Component component = objectMapper.treeToValue(data, clazz);
                    if (component instanceof TransformComponent tc) {
                        tc.x = x;
                        tc.y = y;
                    }
                    entityManager.addComponent(entity, component);
                }
            }
            return entity;
        } catch (Exception e) {
            Gdx.app.error("EntityFactory", "Failed to load entity from " + internalPath, e);
            return null;
        }
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
        entityManager.addComponent(player, new WeaponComponent(WeaponComponent.Type.SHOTGUN, 0.2f, 600f, 10f, 3));
        entityManager.addComponent(player, new TransformComponent(x, y));
        entityManager.addComponent(player, new VelocityComponent(0, 0));
        entityManager.addComponent(player, new RenderComponent(Color.GREEN, 15f, true));
        entityManager.addComponent(player, new ColliderComponent(15f));
        entityManager.addComponent(player, new HealthComponent(100));
        entityManager.addComponent(player, new ScoreComponent());
        return player;
    }
}
