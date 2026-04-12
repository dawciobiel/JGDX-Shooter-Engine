package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;

public class AIComponent implements Component {
    public enum Behavior { STATIONARY, CHASE, WANDER }
    public Behavior behavior;
    public float detectRange;

    public AIComponent() {} // Required for JSON
    public AIComponent(Behavior behavior, float detectRange) {
        this.behavior = behavior;
        this.detectRange = detectRange;
    }
}
