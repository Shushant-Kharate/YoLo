package com.orbitguard.model;

import java.util.List;

public record PlanResponse(List<GridPoint> path, int cost, boolean reachable, String algorithm) {
}
