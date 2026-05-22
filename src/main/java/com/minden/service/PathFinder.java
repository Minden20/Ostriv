package com.minden.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.minden.entity.MapTile;

public class PathFinder {

    public List<String> findPath(MapTile[][] map, MapTile start, MapTile end) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<MapTile> closedSet = new HashSet<>();

        Map<MapTile, PathNode> cameFrom = new HashMap<>();

        PathNode startP = new PathNode(start);
        PathNode endP = new PathNode(end);
        cameFrom.put(start, startP);
        cameFrom.put(end, endP);

        startP.setGCost(0);
        startP.setHCost(calculateHeuristic(start, end));
        startP.calculateFCost();

        openSet.offer(startP);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            if (current.getTile().equals(end)) {
                return reconstructPath(cameFrom, current);
            }

            closedSet.add(current.getTile());

            for (MapTile neighbor : getNeighbors(map, current.getTile())) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGCost = current.getGCost() + neighbor.getMovementCost();

                PathNode neighborP = cameFrom.getOrDefault(neighbor, new PathNode(neighbor));
                if (tentativeGCost < neighborP.getGCost()) {
                    neighborP.setParent(current);
                    cameFrom.put(neighbor, neighborP);
                    neighborP.setGCost(tentativeGCost);
                    neighborP.setHCost(calculateHeuristic(neighbor, end));
                    neighborP.calculateFCost();

                    if (openSet.contains(neighborP)) {
                        openSet.remove(neighborP);
                    }
                    openSet.offer(neighborP);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> reconstructPath(Map<MapTile, PathNode> cameFrom, PathNode current) {
        List<MapTile> pathTiles = retracePath(null, current);
        List<String> path = new ArrayList<>();
        for (MapTile tile : pathTiles) {
            path.add(tile.getX() + "," + tile.getY());
        }
        return path;
    }

    private int calculateHeuristic(MapTile a, MapTile b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private List<MapTile> retracePath(PathNode start, PathNode end) {
        List<MapTile> path = new ArrayList<>();
        PathNode current = end;
        while (current != null) {
            path.add(current.getTile());
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    private List<MapTile> getNeighbors(MapTile[][] map, MapTile tile) {
        List<MapTile> neighbors = new ArrayList<>();
        int x = tile.getX();
        int y = tile.getY();

        if (x > 0) {
            neighbors.add(map[x - 1][y]);
        }
        if (x < map.length - 1) {
            neighbors.add(map[x + 1][y]);
        }
        if (y > 0) {
            neighbors.add(map[x][y - 1]);
        }
        if (y < map[0].length - 1) {
            neighbors.add(map[x][y + 1]);
        }

        return neighbors;
    }
}
