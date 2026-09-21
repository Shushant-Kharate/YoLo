package com.orbitguard.model;

import java.util.List;

public record Detection(
        String id,
        String label,
        double confidence,
        String color,
        List<Double> box
) {
}
