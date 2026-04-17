package pl.shooter.engine.ai.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.components.ObstacleComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.world.GameMap;
import pl.shooter.engine.world.JsonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationGraph implements IndexedGraph<Node> {
    private final GameMap map;
    private final Array<Node> nodes;
    private final Map<String, Node> nodeMap;
    private final int range = 40; // Reduced range for performance, 40 nodes around center is plenty
    private final int tileSize;

    public NavigationGraph(GameMap map) {
        this.map = map;
        this.nodes = new Array<>();
        this.nodeMap = new HashMap<>();
        
        if (map instanceof JsonMap jm) {
            this.tileSize = jm.getDisplaySize();
        } else {
            this.tileSize = 32;
        }
    }

    public void update(float centerX, float centerY, EntityManager entityManager) {
        nodes.clear();
        nodeMap.clear();

        int gridCenterX = (int) Math.floor(centerX / tileSize);
        int gridCenterY = (int) Math.floor(centerY / tileSize);

        // Pre-cache obstacles positions
        Map<String, Boolean> obstacleTiles = new HashMap<>();
        if (entityManager != null) {
            List<Entity> obstacles = entityManager.getEntitiesWithComponents(TransformComponent.class, ObstacleComponent.class);
            for (Entity e : obstacles) {
                TransformComponent t = entityManager.getComponent(e, TransformComponent.class);
                int gx = (int) Math.floor(t.x / tileSize);
                int gy = (int) Math.floor(t.y / tileSize);
                obstacleTiles.put(gx + "," + gy, true);
                
                // Also mark adjacent tiles as blocked if the obstacle is large?
                // For now, 1:1 mapping
            }
        }

        int index = 0;
        float offset = tileSize / 2f;
        for (int x = gridCenterX - range; x <= gridCenterX + range; x++) {
            for (int y = gridCenterY - range; y <= gridCenterY + range; y++) {
                if (map.isWalkable(x * tileSize + offset, y * tileSize + offset) && !obstacleTiles.containsKey(x + "," + y)) {
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
        float offset = tileSize / 2f;
        int[][] cardinal = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] n : cardinal) {
            Node to = nodeMap.get((from.x + n[0]) + "," + (from.y + n[1]));
            if (to != null) {
                float speedMult = map.getSpeedMultiplier(to.x * tileSize + offset, to.y * tileSize + offset);
                from.getConnections().add(new NodeConnection(from, to, 1.0f / Math.max(0.1f, speedMult)));
            }
        }
        
        int[][] diagonal = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int[] n : diagonal) {
            if (nodeMap.containsKey((from.x + n[0]) + "," + from.y) && 
                nodeMap.containsKey(from.x + "," + (from.y + n[1]))) {
                Node to = nodeMap.get((from.x + n[0]) + "," + (from.y + n[1]));
                if (to != null) {
                    float speedMult = map.getSpeedMultiplier(to.x * tileSize + offset, to.y * tileSize + offset);
                    from.getConnections().add(new NodeConnection(from, to, 1.414f / Math.max(0.1f, speedMult)));
                }
            }
        }
    }

    public Node getNodeAt(float worldX, float worldY) {
        int gx = (int) Math.floor(worldX / tileSize);
        int gy = (int) Math.floor(worldY / tileSize);
        
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
