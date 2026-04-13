package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;

/**
 * Connection between two adjacent nodes in the navigation graph.
 */
public class NodeConnection extends DefaultConnection<Node> {
    private final float cost;

    public NodeConnection(Node fromNode, Node toNode, float cost) {
        super(fromNode, toNode);
        this.cost = cost;
    }

    @Override
    public float getCost() {
        return cost;
    }
}
