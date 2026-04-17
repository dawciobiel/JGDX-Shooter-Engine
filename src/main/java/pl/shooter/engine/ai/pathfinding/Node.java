package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

/**
 * A single node (tile) in the navigation graph.
 * Supports clearance-based pathfinding for different entity sizes.
 */
public class Node {
    public final int x, y;
    public final int index; 
    private final Array<Connection<Node>> connections;
    
    // Clearance value: the size of the largest square (in tiles) 
    // starting from this node towards positive X and Y that is fully walkable.
    public int clearance = 1;

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
