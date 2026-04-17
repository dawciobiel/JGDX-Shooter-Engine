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
    private final int range = 45;
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

        Map<String, Boolean> obstacleTiles = new HashMap<>();
        if (entityManager != null) {
            List<Entity> obstacles = entityManager.getEntitiesWithComponents(TransformComponent.class, ObstacleComponent.class);
            for (Entity e : obstacles) {
                TransformComponent t = entityManager.getComponent(e, TransformComponent.class);
                int gx = (int) Math.floor(t.x / tileSize);
                int gy = (int) Math.floor(t.y / tileSize);
                obstacleTiles.put(gx + "," + gy, true);
            }
        }

        int index = 0;
        float offset = tileSize / 2f;
        // Padding for map walls - AI should avoid standing exactly on a wall boundary
        float margin = 4f; 

        for (int x = gridCenterX - range; x <= gridCenterX + range; x++) {
            for (int y = gridCenterY - range; y <= gridCenterY + range; y++) {
                float wx = x * tileSize + offset;
                float wy = y * tileSize + offset;

                if (!obstacleTiles.containsKey(x + "," + y) && isTileFullyWalkable(wx, wy, margin)) {
                    Node node = new Node(x, y, index++);
                    nodes.add(node);
                    nodeMap.put(x + "," + y, node);
                }
            }
        }

        computeClearance();

        for (Node node : nodes) {
            addConnections(node);
        }
    }

    private boolean isTileFullyWalkable(float x, float y, float margin) {
        return map.isWalkable(x, y) &&
               map.isWalkable(x + margin, y + margin) &&
               map.isWalkable(x - margin, y + margin) &&
               map.isWalkable(x + margin, y - margin) &&
               map.isWalkable(x - margin, y - margin);
    }

    private void computeClearance() {
        for (Node node : nodes) {
            int maxClearance = 6; 
            boolean possible = true;
            int c = 1;
            
            while (c < maxClearance && possible) {
                for (int dx = 0; dx <= c; dx++) {
                    for (int dy = 0; dy <= c; dy++) {
                        if (!nodeMap.containsKey((node.x + dx) + "," + (node.y + dy))) {
                            possible = false;
                            break;
                        }
                    }
                    if (!possible) break;
                }
                if (possible) c++;
            }
            node.clearance = c * tileSize;
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
            // CRITICAL FIX: Only allow diagonal if BOTH cardinal neighbors are free.
            // This prevents AI from "cutting corners" and getting stuck.
            Node side1 = nodeMap.get((from.x + n[0]) + "," + from.y);
            Node side2 = nodeMap.get(from.x + "," + (from.y + n[1]));
            Node to = nodeMap.get((from.x + n[0]) + "," + (from.y + n[1]));
            
            if (to != null && side1 != null && side2 != null) {
                float speedMult = map.getSpeedMultiplier(to.x * tileSize + offset, to.y * tileSize + offset);
                from.getConnections().add(new NodeConnection(from, to, 1.414f / Math.max(0.1f, speedMult)));
            }
        }
    }

    public Node getNodeAt(float worldX, float worldY) {
        int gx = (int) Math.floor(worldX / tileSize);
        int gy = (int) Math.floor(worldY / tileSize);
        return nodeMap.get(gx + "," + gy);
    }

    @Override public int getIndex(Node node) { return node.getIndex(); }
    @Override public int getNodeCount() { return nodes.size; }
    @Override public Array<Connection<Node>> getConnections(Node fromNode) { return fromNode.getConnections(); }
}
