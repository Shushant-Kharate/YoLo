package com.orbitguard.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.orbitguard.model.Detection;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class YoloOnnxService {
    private static final int INPUT_SIZE = 640;
    private static final double NMS_IOU_THRESHOLD = 0.45;

    private final OrtEnvironment environment = OrtEnvironment.getEnvironment();
    private final Path modelPath = resolveProjectFile("models/best.onnx");
    private final Path classesPath = resolveProjectFile("models/classes.txt");
    private volatile OrtSession session;
    private volatile List<String> classNames;

    public boolean isModelAvailable() {
        return Files.isRegularFile(modelPath);
    }

    public String modelName() {
        return isModelAvailable() ? modelPath.getFileName().toString() : null;
    }

    public List<Detection> analyze(byte[] payload, double confidenceThreshold) throws IOException, OrtException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(payload));
        if (source == null) {
            throw new IllegalArgumentException("The uploaded file is not a valid image");
        }

        float[] input = preprocess(source);
        long[] shape = {1, 3, INPUT_SIZE, INPUT_SIZE};
        try (OnnxTensor tensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(input), shape);
             OrtSession.Result result = getSession().run(Map.of(inputName(), tensor))) {
            List<Candidate> candidates = parseOutput(result.get(0), confidenceThreshold);
            return nonMaximumSuppression(candidates).stream()
                    .map(this::toDetection)
                    .toList();
        }
    }

    private float[] preprocess(BufferedImage source) {
        BufferedImage resized = new BufferedImage(INPUT_SIZE, INPUT_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, INPUT_SIZE, INPUT_SIZE);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, INPUT_SIZE, INPUT_SIZE, null);
        graphics.dispose();

        int plane = INPUT_SIZE * INPUT_SIZE;
        float[] data = new float[3 * plane];
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) {
                int rgb = resized.getRGB(x, y);
                int offset = y * INPUT_SIZE + x;
                data[offset] = ((rgb >> 16) & 0xFF) / 255.0f;
                data[plane + offset] = ((rgb >> 8) & 0xFF) / 255.0f;
                data[(2 * plane) + offset] = (rgb & 0xFF) / 255.0f;
            }
        }
        return data;
    }

    private List<Candidate> parseOutput(OnnxValue output, double threshold) throws OrtException {
        Object value = output.getValue();
        float[][] rows;
        if (value instanceof float[][][] batched) {
            rows = batched[0];
        } else if (value instanceof float[][] matrix) {
            rows = matrix;
        } else {
            throw new IllegalStateException("Unsupported YOLO output shape. Export a YOLOv5 ONNX model with a [1,N,classes+5] or [1,N,6] output.");
        }

        List<Candidate> candidates = new ArrayList<>();
        for (float[] row : rows) {
            if (row.length < 6) {
                continue;
            }
            Candidate candidate = row.length == 6 ? parseNmsRow(row) : parseRawRow(row);
            if (candidate.confidence >= threshold && candidate.width > 0 && candidate.height > 0) {
                candidates.add(candidate);
            }
        }
        candidates.sort(Comparator.comparingDouble((Candidate c) -> c.confidence).reversed());
        return candidates;
    }

    private Candidate parseNmsRow(float[] row) {
        return new Candidate(row[0], row[1], row[2] - row[0], row[3] - row[1], row[4], Math.round(row[5]));
    }

    private Candidate parseRawRow(float[] row) {
        int bestClass = 0;
        float bestClassScore = 0;
        for (int index = 5; index < row.length; index++) {
            if (row[index] > bestClassScore) {
                bestClassScore = row[index];
                bestClass = index - 5;
            }
        }
        float confidence = row[4] * bestClassScore;
        float x = row[0] - (row[2] / 2f);
        float y = row[1] - (row[3] / 2f);
        return new Candidate(x, y, row[2], row[3], confidence, bestClass);
    }

    private List<Candidate> nonMaximumSuppression(List<Candidate> candidates) {
        List<Candidate> selected = new ArrayList<>();
        for (Candidate candidate : candidates) {
            boolean overlaps = selected.stream()
                    .filter(existing -> existing.classId == candidate.classId)
                    .anyMatch(existing -> intersectionOverUnion(existing, candidate) > NMS_IOU_THRESHOLD);
            if (!overlaps) {
                selected.add(candidate);
            }
        }
        return selected;
    }

    private double intersectionOverUnion(Candidate a, Candidate b) {
        double left = Math.max(a.x, b.x);
        double top = Math.max(a.y, b.y);
        double right = Math.min(a.x + a.width, b.x + b.width);
        double bottom = Math.min(a.y + a.height, b.y + b.height);
        double intersection = Math.max(0, right - left) * Math.max(0, bottom - top);
        double union = (a.width * a.height) + (b.width * b.height) - intersection;
        return union <= 0 ? 0 : intersection / union;
    }

    private Detection toDetection(Candidate candidate) {
        String label = className(candidate.classId);
        String color = label.toLowerCase(Locale.ROOT).contains("debris") ? "amber" : "cyan";
        return new Detection(
                "live-" + candidate.classId + "-" + Math.round(candidate.x) + "-" + Math.round(candidate.y),
                titleCase(label),
                round(candidate.confidence, 4),
                color,
                List.of(
                        round(clamp(candidate.x / INPUT_SIZE * 100), 2),
                        round(clamp(candidate.y / INPUT_SIZE * 100), 2),
                        round(clamp(candidate.width / INPUT_SIZE * 100), 2),
                        round(clamp(candidate.height / INPUT_SIZE * 100), 2)
                )
        );
    }

    private synchronized OrtSession getSession() throws OrtException {
        if (session == null) {
            session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
        }
        return session;
    }

    private String inputName() throws OrtException {
        return getSession().getInputNames().iterator().next();
    }

    private String className(int classId) {
        List<String> names = getClassNames();
        return classId >= 0 && classId < names.size() ? names.get(classId) : "class_" + classId;
    }

    private List<String> getClassNames() {
        if (classNames == null) {
            synchronized (this) {
                if (classNames == null) {
                    try {
                        classNames = Files.isRegularFile(classesPath)
                                ? Files.readAllLines(classesPath).stream().map(String::trim).filter(s -> !s.isEmpty()).toList()
                                : List.of();
                    } catch (IOException exception) {
                        classNames = List.of();
                    }
                }
            }
        }
        return classNames;
    }

    private static Path resolveProjectFile(String relativePath) {
        Path direct = Path.of(relativePath).toAbsolutePath().normalize();
        if (Files.exists(direct)) {
            return direct;
        }
        return Path.of("..", relativePath).toAbsolutePath().normalize();
    }

    private static String titleCase(String value) {
        String[] parts = value.replace('_', ' ').split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (!result.isEmpty()) result.append(' ');
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(100, value));
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    @PreDestroy
    void close() throws OrtException {
        if (session != null) {
            session.close();
        }
    }

    private record Candidate(double x, double y, double width, double height, double confidence, int classId) {
    }
}
