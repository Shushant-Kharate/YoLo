package com.orbitguard.service;

import com.orbitguard.model.GridPoint;
import com.orbitguard.model.PlanRequest;
import com.orbitguard.model.PlanResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AStarPlanningServiceTest {
    private final AStarPlanningService service = new AStarPlanningService();

    @Test
    void findsShortestRouteAroundObstacles() {
        PlanResponse result = service.plan(new PlanRequest(
                6,
                5,
                new GridPoint(0, 2),
                new GridPoint(5, 2),
                List.of(new GridPoint(2, 2), new GridPoint(3, 2))
        ));

        assertThat(result.reachable()).isTrue();
        assertThat(result.cost()).isEqualTo(7);
        assertThat(result.path()).startsWith(new GridPoint(0, 2)).endsWith(new GridPoint(5, 2));
        assertThat(result.path()).doesNotContain(new GridPoint(2, 2), new GridPoint(3, 2));
    }

    @Test
    void returnsEmptyPathWhenGoalIsBlockedOff() {
        PlanResponse result = service.plan(new PlanRequest(
                3,
                3,
                new GridPoint(0, 1),
                new GridPoint(2, 1),
                List.of(new GridPoint(1, 0), new GridPoint(1, 1), new GridPoint(1, 2))
        ));

        assertThat(result.reachable()).isFalse();
        assertThat(result.path()).isEmpty();
        assertThat(result.cost()).isZero();
    }

    @Test
    void rejectsObstacleOnStartPoint() {
        PlanRequest request = new PlanRequest(
                3,
                3,
                new GridPoint(0, 0),
                new GridPoint(2, 2),
                List.of(new GridPoint(0, 0))
        );

        assertThatThrownBy(() -> service.plan(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be obstacles");
    }
}
