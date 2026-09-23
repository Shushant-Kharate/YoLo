package com.orbitguard.service;

import com.orbitguard.model.GridPoint;
import com.orbitguard.model.PlanRequest;
import com.orbitguard.model.PlanResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Service
public class AStarPlanningService {
    private static final int MAX_GRID_SIZE = 200;
    private static final List<GridPoint> DIRECTIONS = List.of(
            new GridPoint(1, 0),
            new GridPoint(-1, 0),
            new GridPoint(0, 1),
            new GridPoint(0, -1)
    );

    public PlanResponse plan(PlanRequest request) {
        validate(request);

        Set<GridPoint> blocked = new HashSet<>(request.obstacles() == null ? List.of() : request.obstacles());
        PriorityQueue<SearchNode> open = new PriorityQueue<>(
                Comparator.comparingInt(SearchNode::estimatedTotalCost)
                        .thenComparingLong(SearchNode::sequence)
        );
        Map<GridPoint, GridPoint> cameFrom = new HashMap<>();
        Map<GridPoint, Integer> bestCost = new HashMap<>();
        long sequence = 0;

        bestCost.put(request.start(), 0);
        open.add(new SearchNode(request.start(), 0, heuristic(request.start(), request.goal()), sequence++));

        while (!open.isEmpty()) {
            SearchNode current = open.poll();
            if (current.cost() != bestCost.getOrDefault(current.point(), Integer.MAX_VALUE)) {
                continue;
            }
            if (current.point().equals(request.goal())) {
                List<GridPoint> path = reconstructPath(cameFrom, request.goal());
                return new PlanResponse(path, path.size() - 1, true, "A*");
            }

            for (GridPoint direction : DIRECTIONS) {
                GridPoint next = new GridPoint(
                        current.point().x() + direction.x(),
                        current.point().y() + direction.y()
                );
                if (!inside(next, request.width(), request.height()) || blocked.contains(next)) {
                    continue;
                }

                int nextCost = current.cost() + 1;
                if (nextCost >= bestCost.getOrDefault(next, Integer.MAX_VALUE)) {
                    continue;
                }

                bestCost.put(next, nextCost);
                cameFrom.put(next, current.point());
                open.add(new SearchNode(next, nextCost, nextCost + heuristic(next, request.goal()), sequence++));
            }
        }

        return new PlanResponse(List.of(), 0, false, "A*");
    }

    private void validate(PlanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Planning request is required");
        }
        if (request.width() <= 0 || request.height() <= 0
                || request.width() > MAX_GRID_SIZE || request.height() > MAX_GRID_SIZE) {
            throw new IllegalArgumentException("Grid dimensions must be between 1 and " + MAX_GRID_SIZE);
        }
        if (request.start() == null || request.goal() == null) {
            throw new IllegalArgumentException("Start and goal points are required");
        }
        if (!inside(request.start(), request.width(), request.height())
                || !inside(request.goal(), request.width(), request.height())) {
            throw new IllegalArgumentException("Start and goal must be inside the grid");
        }
        List<GridPoint> obstacles = request.obstacles() == null ? List.of() : request.obstacles();
        if (obstacles.stream().anyMatch(point -> point == null || !inside(point, request.width(), request.height()))) {
            throw new IllegalArgumentException("Every obstacle must be inside the grid");
        }
        if (obstacles.contains(request.start()) || obstacles.contains(request.goal())) {
            throw new IllegalArgumentException("Start and goal cannot be obstacles");
        }
    }

    private boolean inside(GridPoint point, int width, int height) {
        return point.x() >= 0 && point.y() >= 0 && point.x() < width && point.y() < height;
    }

    private int heuristic(GridPoint point, GridPoint goal) {
        return Math.abs(goal.x() - point.x()) + Math.abs(goal.y() - point.y());
    }

    private List<GridPoint> reconstructPath(Map<GridPoint, GridPoint> cameFrom, GridPoint goal) {
        LinkedList<GridPoint> path = new LinkedList<>();
        GridPoint current = goal;
        path.addFirst(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }
        return new ArrayList<>(path);
    }

    private record SearchNode(GridPoint point, int cost, int estimatedTotalCost, long sequence) {
    }
}
