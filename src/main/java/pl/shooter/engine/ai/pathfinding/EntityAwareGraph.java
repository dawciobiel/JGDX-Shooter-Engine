package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

/**
 * A decorator for NavigationGraph that filters connections based on entity radius.
 */
public class EntityAwareGraph implements IndexedGraph<Node> {
    private final NavigationGraph baseGraph;
    private final float entityRadius;
    private final Array<Connection<Node>> filteredConnections = new Array<>();

    public EntityAwareGraph(NavigationGraph baseGraph, float entityRadius) {
        this.baseGraph = baseGraph;
        this.entityRadius = entityRadius;
    }

    @Override
    public Array<Connection<Node>> getConnections(Node fromNode) {
        filteredConnections.clear();
        Array<Connection<Node>> baseConnections = baseGraph.getConnections(fromNode);
        
        for (Connection<Node> conn : baseConnections) {
            // Only allow transition if the destination node can fit the entity.
            // We use radius * 2 to represent the full width of the entity.
            if (conn.getToNode().clearance >= entityRadius * 2) {
                filteredConnections.add(conn);
            }
        }
        return filteredConnections;
    }

    @Override
    public int getIndex(Node node) {
        return baseGraph.getIndex(node);
    }

    @Override
    public int getNodeCount() {
        return baseGraph.getNodeCount();
    }
}
