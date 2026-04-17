package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import pl.shooter.engine.ai.pathfinding.DistanceHeuristic;
import pl.shooter.engine.ai.pathfinding.NavigationGraph;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class PathfindingSystem extends GameSystem {
    private final NavigationGraph graph;
    private IndexedAStarPathFinder<Node> pathFinder;
    private final DistanceHeuristic heuristic;
    private float graphRefreshTimer = 0;
    private int lastObstacleCount = -1;
    private float lastCenterX = -1000;
    private float lastCenterY = -1000;

    public PathfindingSystem(EntityManager entityManager, GameMap map) {
        super(entityManager);
        this.graph = new NavigationGraph(map);
        this.heuristic = new DistanceHeuristic();
    }

    @Override
    public void update(float deltaTime) {
        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        if (players.isEmpty()) return;

        Entity player = players.get(0);
        TransformComponent playerTrans = entityManager.getComponent(player, TransformComponent.class);

        // Refresh graph if objects were destroyed or player moved significantly
        graphRefreshTimer += deltaTime;
        
        float distToLastCenter = (float) Math.sqrt(Math.pow(playerTrans.x - lastCenterX, 2) + Math.pow(playerTrans.y - lastCenterY, 2));
        int currentObstacles = entityManager.getEntitiesWithComponents(ObstacleComponent.class).size();

        if (graphRefreshTimer > 1.0f || distToLastCenter > 200f || currentObstacles != lastObstacleCount) {
            graph.update(playerTrans.x, playerTrans.y, entityManager);
            this.pathFinder = new IndexedAStarPathFinder<>(graph);
            this.lastObstacleCount = currentObstacles;
            this.lastCenterX = playerTrans.x;
            this.lastCenterY = playerTrans.y;
            graphRefreshTimer = 0;
        }

        if (pathFinder == null || graph.getNodeCount() == 0) return;

        // Update AI paths
        List<Entity> enemies = entityManager.getEntitiesWithComponents(AIComponent.class, TransformComponent.class);
        for (Entity enemy : enemies) {
            AIComponent ai = entityManager.getComponent(enemy, AIComponent.class);
            TransformComponent enemyTrans = entityManager.getComponent(enemy, TransformComponent.class);

            if (ai.behavior == AIComponent.Behavior.CHASE) {
                ai.pathfindingTimer += deltaTime;
                if (ai.pathfindingTimer >= AIComponent.PATHFINDING_RECALC_INTERVAL) {
                    calculatePath(enemyTrans, playerTrans, ai);
                    ai.pathfindingTimer = 0;
                }
            }
        }
    }

    private void calculatePath(TransformComponent start, TransformComponent end, AIComponent ai) {
        Node startNode = graph.getNodeAt(start.x, start.y);
        Node endNode = graph.getNodeAt(end.x, end.y);

        if (startNode != null && endNode != null && startNode != endNode) {
            DefaultGraphPath<Node> tempPath = new DefaultGraphPath<>();
            try {
                pathFinder.searchNodePath(startNode, endNode, heuristic, tempPath);
                if (tempPath.getCount() > 1) {
                    ai.currentPath.clear();
                    for (int i = 0; i < tempPath.getCount(); i++) {
                        ai.currentPath.add(tempPath.get(i));
                    }
                }
            } catch (Exception e) {}
        }
    }
}
