package com.orbitguard.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoDetectionServiceTest {
    private final DemoDetectionService service = new DemoDetectionService();

    @Test
    void filtersDetectionsByConfidence() {
        assertThat(service.analyze("leo", 0.90))
                .hasSize(1)
                .first()
                .extracting("label")
                .isEqualTo("Satellite");
    }

    @Test
    void fallsBackToUploadScene() {
        assertThat(service.analyze("unknown", 0.5))
                .hasSize(1)
                .first()
                .extracting("label")
                .isEqualTo("Unverified object");
    }
}
