package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

public class HealthComponent implements Component {
    public float hp;
    public float maxHp;

    public HealthComponent() {} // Required for JSON
    public HealthComponent(float hp) {
        this.hp = hp;
        this.maxHp = hp;
    }
}
