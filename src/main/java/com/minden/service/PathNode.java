package com.minden.service;

import com.minden.entity.MapTile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PathNode implements Comparable<PathNode> {

    private final MapTile tile;
    private PathNode parent;

    private int gCost = Integer.MAX_VALUE;
    private int hCost;
    private int fCost;

    public PathNode(MapTile tile) {
        this.tile = tile;
    }

    public void calculateFCost() {
        this.fCost = this.gCost + this.hCost;
    }

    @Override
    public int compareTo(PathNode other) {
        return Integer.compare(this.fCost, other.fCost);
    }
}
