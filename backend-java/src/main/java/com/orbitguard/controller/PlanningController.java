package com.orbitguard.controller;

import com.orbitguard.model.PlanRequest;
import com.orbitguard.model.PlanResponse;
import com.orbitguard.service.AStarPlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class PlanningController {
    private final AStarPlanningService planningService;

    public PlanningController(AStarPlanningService planningService) {
        this.planningService = planningService;
    }

    @PostMapping("/plan")
    public PlanResponse plan(@RequestBody PlanRequest request) {
        try {
            return planningService.plan(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
