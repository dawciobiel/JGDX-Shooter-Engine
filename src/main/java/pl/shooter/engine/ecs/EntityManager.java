package pl.shooter.engine.ecs;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages all entities and their components.
 */
public class EntityManager {
    private final AtomicInteger nextEntityId = new AtomicInteger(1);
    private final Set<Entity> entities = new HashSet<>();
    private final Map<Integer, Map<Class<? extends Component>, Component>> componentsByEntity = new HashMap<>();

    /**
     * Creates a new entity with a unique ID.
     */
    public Entity createEntity() {
        Entity entity = new Entity(nextEntityId.getAndIncrement());
        entities.add(entity);
        componentsByEntity.put(entity.getId(), new HashMap<>());
        return entity;
    }

    public Entity getEntityById(int id) {
        for (Entity e : entities) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    /**
     * Adds a component to an entity.
     */
    public <T extends Component> void addComponent(Entity entity, T component) {
        Map<Class<? extends Component>, Component> entityComponents = componentsByEntity.get(entity.getId());
        if (entityComponents != null) {
            entityComponents.put(component.getClass(), component);
        }
    }

    /**
     * Gets a component from an entity.
     */
    public <T extends Component> T getComponent(Entity entity, Class<T> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = componentsByEntity.get(entity.getId());
        if (entityComponents == null) return null;
        return componentClass.cast(entityComponents.get(componentClass));
    }

    /**
     * Checks if an entity has a specific component.
     */
    public boolean hasComponent(Entity entity, Class<? extends Component> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = componentsByEntity.get(entity.getId());
        return entityComponents != null && entityComponents.containsKey(componentClass);
    }

    /**
     * Removes a specific component from an entity.
     */
    public void removeComponent(Entity entity, Class<? extends Component> componentClass) {
        Map<Class<? extends Component>, Component> entityComponents = componentsByEntity.get(entity.getId());
        if (entityComponents != null) {
            entityComponents.remove(componentClass);
        }
    }

    /**
     * Removes an entity and all its components.
     */
    public void removeEntity(Entity entity) {
        entities.remove(entity);
        componentsByEntity.remove(entity.getId());
    }

    /**
     * Returns a collection of all active entities.
     */
    public Set<Entity> getAllEntities() {
        return Collections.unmodifiableSet(entities);
    }

    /**
     * Returns entities that possess a specific set of components.
     * Useful for Systems to filter entities they need to process.
     */
    public List<Entity> getEntitiesWithComponents(Class<? extends Component>... componentClasses) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            boolean matches = true;
            for (Class<? extends Component> clazz : componentClasses) {
                if (!hasComponent(entity, clazz)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                result.add(entity);
            }
        }
        return result;
    }
}
