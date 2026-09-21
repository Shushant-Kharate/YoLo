package com.orbitguard.model;

import java.util.List;

public record AnalyzeResponse(String mode, List<Detection> detections, String message) {
}
