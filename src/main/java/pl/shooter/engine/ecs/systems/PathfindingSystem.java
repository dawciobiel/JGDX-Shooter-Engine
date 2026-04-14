package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import pl.shooter.engine.ai.pathfinding.DistanceHeuristic;
import pl.shooter.engine.ai.pathfinding.NavigationGraph;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.DestructibleComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class PathfindingSystem extends GameSystem {
    private final NavigationGraph graph;
    private IndexedAStarPathFinder<Node> pathFinder;
    private final DistanceHeuristic heuristic;
    private float graphRefreshTimer = 0;
    private int lastObstacleCount = -1;

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

        // Periodically check if we need to refresh the graph (e.g. if a crate was destroyed)
        graphRefreshTimer += deltaTime;
        if (graphRefreshTimer > 1.0f) {
            int currentObstacles = entityManager.getEntitiesWithComponents(DestructibleComponent.class).size();
            if (currentObstacles != lastObstacleCount) {
                graph.update(800, 800, entityManager);
                this.pathFinder = new IndexedAStarPathFinder<>(graph);
                this.lastObstacleCount = currentObstacles;
                Gdx.app.log("PathfindingSystem", "Graph refreshed due to obstacle change.");
            }
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
                    if (ai.currentPathIndex >= ai.currentPath.getCount()) ai.currentPathIndex = 1;
                }
            } catch (Exception e) {}
        }
    }
}
