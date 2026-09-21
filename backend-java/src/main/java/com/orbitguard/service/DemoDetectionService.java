package com.orbitguard.service;

import com.orbitguard.model.Detection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DemoDetectionService {
    private static final Map<String, List<Detection>> SCENES = Map.of(
            "leo", List.of(
                    detection("leo-sat", "Satellite", 0.93, "cyan", 69, 26, 25, 43),
                    detection("leo-debris", "Debris", 0.87, "amber", 45, 14, 8, 10)
            ),
            "station", List.of(
                    detection("station-main", "Satellite", 0.96, "cyan", 4, 5, 54, 66),
                    detection("station-debris-a", "Debris", 0.89, "amber", 75, 24, 9, 17),
                    detection("station-debris-b", "Debris", 0.81, "amber", 85, 51, 6, 12)
            ),
            "field", List.of(
                    detection("field-sat", "Satellite", 0.91, "cyan", 78, 4, 14, 16),
                    detection("field-a", "Debris", 0.88, "amber", 6, 28, 10, 23),
                    detection("field-b", "Debris", 0.84, "amber", 55, 45, 6, 11),
                    detection("field-c", "Debris", 0.78, "amber", 87, 56, 8, 20),
                    detection("field-d", "Debris", 0.72, "amber", 31, 16, 5, 8)
            ),
            "upload", List.of(
                    detection("upload-object", "Unverified object", 0.68, "amber", 38, 28, 22, 28)
            )
    );

    public List<Detection> analyze(String scene, double threshold) {
        return SCENES.getOrDefault(scene, SCENES.get("upload")).stream()
                .filter(detection -> detection.confidence() >= threshold)
                .toList();
    }

    private static Detection detection(String id, String label, double confidence, String color,
                                       double x, double y, double width, double height) {
        return new Detection(id, label, confidence, color, List.of(x, y, width, height));
    }
}
