package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Component;

public class AIComponent implements Component {
    public enum Behavior { STATIONARY, CHASE, WANDER }
    public Behavior behavior;
    public float detectRange;
    
    // --- New Tactical Parameters ---
    public float preferredRange = 0;    // 0 = close melee, >0 = keep distance
    public boolean stopToShoot = false; // should agent stop when firing?

    // Pathfinding data
    public GraphPath<Node> currentPath = new DefaultGraphPath<>();
    public float pathfindingTimer = 0; 
    public static final float PATHFINDING_RECALC_INTERVAL = 0.1f; 
    public int currentPathIndex = 0;

    public AIComponent() {}
    public AIComponent(Behavior behavior, float detectRange) {
        this.behavior = behavior;
        this.detectRange = detectRange;
    }
}
