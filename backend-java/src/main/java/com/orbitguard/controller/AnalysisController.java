package com.orbitguard.controller;

import ai.onnxruntime.OrtException;
import com.orbitguard.model.AnalyzeResponse;
import com.orbitguard.model.Detection;
import com.orbitguard.model.HealthResponse;
import com.orbitguard.service.DemoDetectionService;
import com.orbitguard.service.YoloOnnxService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final DemoDetectionService demoDetectionService;
    private final YoloOnnxService yoloOnnxService;

    public AnalysisController(DemoDetectionService demoDetectionService, YoloOnnxService yoloOnnxService) {
        this.demoDetectionService = demoDetectionService;
        this.yoloOnnxService = yoloOnnxService;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        boolean live = yoloOnnxService.isModelAvailable();
        return new HealthResponse("ok", live ? "live" : "demo", yoloOnnxService.modelName(), "Java 21 + Spring Boot");
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalyzeResponse analyze(
            @RequestParam(defaultValue = "0.5") double confidence,
            @RequestParam(defaultValue = "upload") String scene,
            @RequestParam(required = false) MultipartFile file
    ) {
        if (confidence < 0 || confidence > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confidence must be between 0 and 1");
        }

        if (!yoloOnnxService.isModelAvailable()) {
            List<Detection> detections = demoDetectionService.analyze(scene, confidence);
            return new AnalyzeResponse("demo", detections,
                    "Bundled demonstration detections. Add models/best.onnx for Java ONNX inference.");
        }

        validateFile(file);
        try {
            return new AnalyzeResponse("live", yoloOnnxService.analyze(file.getBytes(), confidence),
                    "Live YOLO inference completed in Java with ONNX Runtime.");
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IOException | OrtException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Java ONNX inference failed: " + exception.getMessage(), exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload an image for live inference");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image exceeds 10 MB");
        }
        if (!MediaType.IMAGE_JPEG_VALUE.equals(file.getContentType())
                && !MediaType.IMAGE_PNG_VALUE.equals(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPG and PNG images are supported");
        }
    }
}
