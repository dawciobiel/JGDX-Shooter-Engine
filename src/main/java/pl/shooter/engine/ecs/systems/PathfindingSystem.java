package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import pl.shooter.engine.ai.pathfinding.DistanceHeuristic;
import pl.shooter.engine.ai.pathfinding.NavigationGraph;
import pl.shooter.engine.ai.pathfinding.Node;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.AIComponent;
import pl.shooter.engine.ecs.components.PlayerComponent;
import pl.shooter.engine.ecs.components.TransformComponent;
import pl.shooter.engine.world.GameMap;

import java.util.List;

public class PathfindingSystem extends GameSystem {
    private final NavigationGraph graph;
    private IndexedAStarPathFinder<Node> pathFinder;
    private final DistanceHeuristic heuristic;
    private float graphUpdateTimer = 1.0f;
    private int lastNodeCount = -1;

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

        // 1. Periodic graph update
        graphUpdateTimer += deltaTime;
        if (graphUpdateTimer > 0.5f) {
            graph.update(playerTrans.x, playerTrans.y);
            graphUpdateTimer = 0;
            
            // Re-create pathfinder if graph size changed (CRITICAL FIX)
            if (graph.getNodeCount() != lastNodeCount) {
                this.pathFinder = new IndexedAStarPathFinder<>(graph);
                this.lastNodeCount = graph.getNodeCount();
            }
        }

        if (pathFinder == null || graph.getNodeCount() == 0) return;

        // 2. Update AI paths
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
            try {
                ai.currentPath.clear();
                pathFinder.searchNodePath(startNode, endNode, heuristic, ai.currentPath);
            } catch (Exception e) {
                ai.currentPath.clear();
            }
        } else {
            // If we can't find nodes, clear the path so AISystem knows we are stuck
            ai.currentPath.clear();
        }
    }
}
