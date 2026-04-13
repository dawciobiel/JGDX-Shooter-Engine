package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

/**
 * A single node (tile) in the navigation graph.
 * Required for gdx-ai pathfinding.
 */
public class Node {
    public final int x, y;
    public final int index; // Unique index for gdx-ai
    private final Array<Connection<Node>> connections;

    public Node(int x, int y, int index) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.connections = new Array<>();
    }

    public int getIndex() {
        return index;
    }

    public Array<Connection<Node>> getConnections() {
        return connections;
    }
}
