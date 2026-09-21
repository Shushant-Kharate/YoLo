package com.orbitguard.controller;

import com.orbitguard.service.DemoDetectionService;
import com.orbitguard.service.YoloOnnxService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new AnalysisController(new DemoDetectionService(), new YoloOnnxService())
    ).build();

    @Test
    void reportsJavaDemoModeWithoutModel() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("demo"))
                .andExpect(jsonPath("$.runtime").value("Java 21 + Spring Boot"));
    }

    @Test
    void returnsFilteredDemoDetections() throws Exception {
        mockMvc.perform(multipart("/api/analyze")
                        .param("scene", "leo")
                        .param("confidence", "0.90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("demo"))
                .andExpect(jsonPath("$.detections.length()").value(1))
                .andExpect(jsonPath("$.detections[0].label").value("Satellite"));
    }
}
