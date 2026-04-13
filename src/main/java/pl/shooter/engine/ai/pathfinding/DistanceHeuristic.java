package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.math.Vector2;

/**
 * Heuristic for A* pathfinding.
 * Estimates cost based on Euclidean distance.
 */
public class DistanceHeuristic implements Heuristic<Node> {
    @Override
    public float estimate(Node node, Node endNode) {
        return Vector2.dst(node.x, node.y, endNode.x, endNode.y);
    }
}
