package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.TestingMap;

import java.util.HashMap;
import java.util.Map;

public class NavigationGraph implements IndexedGraph<Node> {
    private final GameMap map;
    private final Array<Node> nodes;
    private final Map<String, Node> nodeMap;
    // Massive range to cover the whole testing area (50x50 tiles = 1600px)
    private final int range = 100; 

    public NavigationGraph(GameMap map) {
        this.map = map;
        this.nodes = new Array<>();
        this.nodeMap = new HashMap<>();
    }

    public void update(float centerX, float centerY) {
        nodes.clear();
        nodeMap.clear();

        int gridCenterX = (int) Math.floor(centerX / 32);
        int gridCenterY = (int) Math.floor(centerY / 32);

        int index = 0;
        // Search from grid coords -range to +range relative to center
        for (int x = gridCenterX - range; x <= gridCenterX + range; x++) {
            for (int y = gridCenterY - range; y <= gridCenterY + range; y++) {
                // Bounds check against map if possible, but testing map is small anyway
                if (map.isWalkable(x * 32 + 16, y * 32 + 16)) {
                    Node node = new Node(x, y, index++);
                    nodes.add(node);
                    nodeMap.put(x + "," + y, node);
                }
            }
        }

        for (Node node : nodes) {
            addConnections(node);
        }
    }

    private void addConnections(Node from) {
        int[][] cardinal = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] n : cardinal) {
            Node to = nodeMap.get((from.x + n[0]) + "," + (from.y + n[1]));
            if (to != null) {
                float speedMult = map.getSpeedMultiplier(to.x * 32 + 16, to.y * 32 + 16);
                from.getConnections().add(new NodeConnection(from, to, 1.0f / Math.max(0.1f, speedMult)));
            }
        }
        
        int[][] diagonal = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int[] n : diagonal) {
            if (nodeMap.containsKey((from.x + n[0]) + "," + from.y) && 
                nodeMap.containsKey(from.x + "," + (from.y + n[1]))) {
                Node to = nodeMap.get((from.x + n[0]) + "," + (from.y + n[1]));
                if (to != null) {
                    float speedMult = map.getSpeedMultiplier(to.x * 32 + 16, to.y * 32 + 16);
                    from.getConnections().add(new NodeConnection(from, to, 1.414f / Math.max(0.1f, speedMult)));
                }
            }
        }
    }

    public Node getNodeAt(float worldX, float worldY) {
        int gx = (int) Math.floor(worldX / 32);
        int gy = (int) Math.floor(worldY / 32);
        
        // Search slightly wider if exact tile is not a node (e.g. wall or outside range)
        for (int r = 0; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (r > 0 && Math.abs(dx) != r && Math.abs(dy) != r) continue;
                    Node node = nodeMap.get((gx + dx) + "," + (gy + dy));
                    if (node != null) return node;
                }
            }
        }
        return null;
    }

    @Override public int getIndex(Node node) { return node.getIndex(); }
    @Override public int getNodeCount() { return nodes.size; }
    @Override public Array<Connection<Node>> getConnections(Node fromNode) { return fromNode.getConnections(); }
}
