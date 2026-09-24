import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * OrbitGuard's complete Java backend.
 *
 * Compile: javac --add-modules jdk.httpserver Backend.java Frontend.java
 * Run:     java --add-modules jdk.httpserver Backend
 */
public final class Backend {
    private static final int PORT = 8000;
    private static final Path MODEL = Path.of("model.onnx").toAbsolutePath().normalize();
    private static final int MAX_GRID_SIZE = 200;
    private static final List<Point> DIRECTIONS = List.of(
            new Point(1, 0), new Point(-1, 0), new Point(0, 1), new Point(0, -1)
    );

    private Backend() {
    }

    public static void main(String[] args) throws IOException {
        if (!Files.isRegularFile(MODEL)) {
            throw new IOException("model.onnx was not found beside Backend.java");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/", Backend::serveHome);
        server.createContext("/model.onnx", Backend::serveModel);
        server.createContext("/api/health", Backend::serveHealth);
        server.createContext("/api/plan", Backend::servePlan);
        server.setExecutor(Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors())));
        server.start();

        System.out.println("OrbitGuard is running at http://localhost:" + PORT);
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void serveHome(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod()) || !"/".equals(exchange.getRequestURI().getPath())) {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found");
            return;
        }
        send(exchange, 200, "text/html; charset=utf-8", Frontend.PAGE);
    }

    private static void serveModel(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=utf-8", "Method not allowed");
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
        exchange.sendResponseHeaders(200, Files.size(MODEL));
        try (var output = exchange.getResponseBody()) {
            Files.copy(MODEL, output);
        }
    }

    private static void serveHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
            return;
        }
        send(exchange, 200, "application/json; charset=utf-8",
                "{\"status\":\"ok\",\"runtime\":\"Java " + Runtime.version().feature()
                        + "\",\"model\":\"model.onnx\",\"planning\":\"A*\"}");
    }

    private static void servePlan(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "application/json", "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            Map<String, String> form = parseForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            int width = number(form, "width");
            int height = number(form, "height");
            Point start = point(form.get("start"));
            Point goal = point(form.get("goal"));
            Set<Point> obstacles = points(form.getOrDefault("obstacles", ""));
            List<Point> path = aStar(width, height, start, goal, obstacles);
            send(exchange, 200, "application/json; charset=utf-8", pathJson(path));
        } catch (IllegalArgumentException exception) {
            send(exchange, 400, "application/json; charset=utf-8",
                    "{\"error\":\"" + jsonEscape(exception.getMessage()) + "\"}");
        }
    }

    private static List<Point> aStar(int width, int height, Point start, Point goal, Set<Point> obstacles) {
        if (width < 1 || height < 1 || width > MAX_GRID_SIZE || height > MAX_GRID_SIZE) {
            throw new IllegalArgumentException("Grid dimensions must be between 1 and " + MAX_GRID_SIZE);
        }
        if (!inside(start, width, height) || !inside(goal, width, height)) {
            throw new IllegalArgumentException("Start and goal must be inside the grid");
        }
        if (obstacles.stream().anyMatch(point -> !inside(point, width, height))) {
            throw new IllegalArgumentException("Every obstacle must be inside the grid");
        }
        if (obstacles.contains(start) || obstacles.contains(goal)) {
            throw new IllegalArgumentException("Start and goal cannot be obstacles");
        }

        PriorityQueue<Node> open = new PriorityQueue<>(
                Comparator.comparingInt(Node::estimatedTotal).thenComparingLong(Node::sequence)
        );
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Integer> bestCost = new HashMap<>();
        long sequence = 0;

        bestCost.put(start, 0);
        open.add(new Node(start, 0, heuristic(start, goal), sequence++));

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.cost() != bestCost.getOrDefault(current.point(), Integer.MAX_VALUE)) {
                continue;
            }
            if (current.point().equals(goal)) {
                return reconstruct(cameFrom, goal);
            }
            for (Point direction : DIRECTIONS) {
                Point next = new Point(current.point().x() + direction.x(), current.point().y() + direction.y());
                if (!inside(next, width, height) || obstacles.contains(next)) {
                    continue;
                }
                int nextCost = current.cost() + 1;
                if (nextCost >= bestCost.getOrDefault(next, Integer.MAX_VALUE)) {
                    continue;
                }
                bestCost.put(next, nextCost);
                cameFrom.put(next, current.point());
                open.add(new Node(next, nextCost, nextCost + heuristic(next, goal), sequence++));
            }
        }
        return List.of();
    }

    private static List<Point> reconstruct(Map<Point, Point> cameFrom, Point goal) {
        LinkedList<Point> path = new LinkedList<>();
        Point current = goal;
        path.addFirst(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.addFirst(current);
        }
        return new ArrayList<>(path);
    }

    private static int heuristic(Point point, Point goal) {
        return Math.abs(goal.x() - point.x()) + Math.abs(goal.y() - point.y());
    }

    private static boolean inside(Point point, int width, int height) {
        return point.x() >= 0 && point.y() >= 0 && point.x() < width && point.y() < height;
    }

    private static String pathJson(List<Point> path) {
        StringBuilder json = new StringBuilder("{\"reachable\":")
                .append(!path.isEmpty())
                .append(",\"cost\":")
                .append(Math.max(0, path.size() - 1))
                .append(",\"algorithm\":\"A*\",\"path\":[");
        for (int index = 0; index < path.size(); index++) {
            if (index > 0) json.append(',');
            Point point = path.get(index);
            json.append('[').append(point.x()).append(',').append(point.y()).append(']');
        }
        return json.append("]}").toString();
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> result = new HashMap<>();
        if (body.isBlank()) return result;
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private static int number(Map<String, String> form, String key) {
        try {
            return Integer.parseInt(form.getOrDefault(key, ""));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
    }

    private static Point point(String value) {
        try {
            String[] parts = value.split(",", 2);
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid grid point");
        }
    }

    private static Set<Point> points(String value) {
        Set<Point> result = new HashSet<>();
        if (value == null || value.isBlank()) return result;
        for (String item : value.split(";")) result.add(point(item));
        return result;
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String jsonEscape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record Point(int x, int y) {
    }

    private record Node(Point point, int cost, int estimatedTotal, long sequence) {
    }
}
