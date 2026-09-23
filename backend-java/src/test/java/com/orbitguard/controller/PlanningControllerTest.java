package com.orbitguard.controller;

import com.orbitguard.service.AStarPlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanningControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PlanningController(new AStarPlanningService()))
                .build();
    }

    @Test
    void returnsJavaAStarRoute() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "width": 4,
                                  "height": 3,
                                  "start": {"x": 0, "y": 1},
                                  "goal": {"x": 3, "y": 1},
                                  "obstacles": [{"x": 1, "y": 1}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reachable").value(true))
                .andExpect(jsonPath("$.cost").value(5))
                .andExpect(jsonPath("$.algorithm").value("A*"))
                .andExpect(jsonPath("$.path[0].x").value(0))
                .andExpect(jsonPath("$.path[0].y").value(1));
    }

    @Test
    void rejectsInvalidGrid() throws Exception {
        mockMvc.perform(post("/api/plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "width": 0,
                                  "height": 3,
                                  "start": {"x": 0, "y": 1},
                                  "goal": {"x": 2, "y": 1},
                                  "obstacles": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
