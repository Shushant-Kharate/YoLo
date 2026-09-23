package com.orbitguard.model;

import java.util.List;

public record PlanRequest(
        int width,
        int height,
        GridPoint start,
        GridPoint goal,
        List<GridPoint> obstacles
) {
}
