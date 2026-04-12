package pl.shooter.engine.ecs;

import java.util.Map;

/**
 * Data structure representing an entity definition in JSON.
 */
public class EntityTemplate {
    public String type;
    public Map<String, Object> components;
}
