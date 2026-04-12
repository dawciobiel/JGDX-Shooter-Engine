package pl.shooter.engine.ecs;

import java.util.Objects;

/**
 * Represents a single entity in the ECS.
 * An entity is just an ID that connects multiple components.
 */
public final class Entity {
    private final int id;

    public Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Entity{id=" + id + "}";
    }
}
