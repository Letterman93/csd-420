/*
 * Zak Nizam | CSD 420 | Module 11.2 | November 20, 2026
 * Offline checks for JSON parsing, time conversion, and HTTP handling.
 * The historical response fixture comes from N2YO's published visual-passes
 * example at https://www.n2yo.com/api/ . It is not a current prediction.
 * Test-only changes to that response exercise error cases, not satellite forecasts.
 * No key is needed and no test calls N2YO.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class IssVisiblePassTrackerChecks {
    private static int passed;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String json = loadFixture();
        if (args.length == 1 && "--sample".equals(args[0])) {
            System.out.println("Historical N2YO documentation example (March 2018).\n"
                    + "This is parser output, not live Kennedy Space Center predictions.\n"
                    + "Source: https://www.n2yo.com/api/\n");
            System.out.println(IssVisiblePassTracker.formatResponse(json));
            return;
        }
        System.out.println("Zak Nizam | CSD 420 | Module 11.2 | November 20, 2026");
        System.out.println("Offline checks - published historical N2YO data and a local HTTP server");
        String output = IssVisiblePassTracker.formatResponse(json);
        check("Nested info and passes are parsed", output.contains("Satellite: SPACE STATION")
                && output.contains("NORAD ID: 25544") && output.contains("Visible passes found: 3"));
        check("Every pass is displayed", output.contains("Pass 1") && output.contains("Pass 3"));
        check("Directions and elevation are displayed", output.contains("Start direction: NW")
                && output.contains("Maximum elevation: 78.3 degrees")
                && output.contains("Maximum direction: SW") && output.contains("End direction: SE"));
        check("Visible duration uses the API value", output.contains("Visible duration: 8.1 minutes"));
        check("Magnitude is displayed", output.contains("Magnitude: -2.4"));
        check("Winter timestamps use EST", IssVisiblePassTracker.formatTime(
                Instant.parse("2026-01-01T00:00:00Z").getEpochSecond())
                .equals("December 31, 2025 at 7:00:00 PM EST"));
        check("Summer timestamps use EDT", IssVisiblePassTracker.formatTime(
                Instant.parse("2026-07-01T00:00:00Z").getEpochSecond())
                .equals("June 30, 2026 at 8:00:00 PM EDT"));
        ObjectNode empty = tree(json);
        ((ObjectNode) empty.get("info")).put("passescount", 0);
        empty.set("passes", MAPPER.createArrayNode());
        check("Zero passes produces a useful message", IssVisiblePassTracker.formatResponse(empty.toString())
                .contains("No visible passes meet these settings"));
        for (String variant : new String[] {"missing", "null", "sentinel"}) {
            ObjectNode changed = tree(json);
            ObjectNode pass = (ObjectNode) changed.withArray("passes").get(0);
            if (variant.equals("missing")) { pass.remove("mag"); }
            if (variant.equals("null")) { pass.putNull("mag"); }
            if (variant.equals("sentinel")) { pass.put("mag", 100000); }
            check("Unavailable magnitude: " + variant,
                    IssVisiblePassTracker.formatResponse(changed.toString()).contains("Magnitude: unavailable"));
        }
        rejects("Malformed JSON is rejected", "{unfinished");
        rejects("Trailing JSON is rejected", json + " {}");
        rejects("API error response is rejected", "{\"error\":\"API request rejected\"}");
        rejects("Missing info object is rejected", "{\"passes\":[]}");
        rejects("Nonobject JSON is rejected", "[]");
        rejects("Blank JSON is rejected", "");
        ObjectNode missing = tree(json);
        ((ObjectNode) missing.get("info")).remove("satname");
        rejects("Missing satellite name is rejected", missing.toString());
        ObjectNode mismatch = tree(json);
        ((ObjectNode) mismatch.get("info")).put("passescount", 8);
        rejects("Inconsistent pass count is rejected", mismatch.toString());
        ObjectNode badTime = tree(json);
        ((ObjectNode) badTime.withArray("passes").get(0)).put("startUTC", "not a timestamp");
        rejects("Wrong timestamp type is rejected", badTime.toString());
        ObjectNode badDuration = tree(json);
        ((ObjectNode) badDuration.withArray("passes").get(0)).put("duration", -1);
        rejects("Negative duration is rejected", badDuration.toString());
        for (String key : new String[] {null, "", "  "}) {
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            ByteArrayOutputStream normal = new ByteArrayOutputStream();
            int result = IssVisiblePassTracker.run(key, new PrintStream(normal), new PrintStream(errors));
            check("Missing or blank key fails cleanly", result == 1 && normal.size() == 0
                    && errors.toString(StandardCharsets.UTF_8).contains("N2YO_API_KEY is not set"));
        }
        checkHttp(json);
        System.out.println("All " + passed + " checks passed. No live API requests were made.");
    }

    private static void checkHttp(String json) throws Exception {
        AtomicBoolean correctRequest = new AtomicBoolean();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/success", exchange -> {
            correctRequest.set(exchange.getRequestMethod().equals("GET")
                    && "application/json".equals(exchange.getRequestHeaders().getFirst("Accept")));
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var stream = exchange.getResponseBody()) { stream.write(body); }
        });
        server.createContext("/denied", exchange -> {
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();
        // Use a numeric loopback host, not the internet or any account's API key.
        String host = server.getAddress().getAddress().getHostAddress();
        String base = "http://" + (host.contains(":") ? "[" + host + "]" : host)
                + ":" + server.getAddress().getPort();
        try (HttpClient client = HttpClient.newHttpClient()) {
            check("GET request receives and parses a 200 response",
                    IssVisiblePassTracker.formatResponse(IssVisiblePassTracker.fetchJson(
                            client, URI.create(base + "/success"))).contains("Pass 3") && correctRequest.get());
            try {
                IssVisiblePassTracker.fetchJson(client, URI.create(base + "/denied"));
                throw new AssertionError("Expected HTTP 401 to be rejected");
            } catch (IllegalArgumentException expected) {
                check("Non-200 response reports its HTTP status", expected.getMessage().contains("HTTP 401"));
            }
        } finally {
            server.stop(0);
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            try {
                IssVisiblePassTracker.fetchJson(client, URI.create(base + "/success"));
                throw new AssertionError("Expected a connection failure");
            } catch (IOException expected) {
                check("Network connection failure is detected", true);
            }
        }
    }

    private static String loadFixture() throws IOException {
        try (InputStream stream = IssVisiblePassTrackerChecks.class
                .getResourceAsStream("/n2yo-documentation-response.json")) {
            if (stream == null) {
                throw new IOException("Missing test resource: n2yo-documentation-response.json");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static ObjectNode tree(String json) throws IOException {
        return (ObjectNode) MAPPER.readTree(json);
    }

    private static void rejects(String description, String json) throws Exception {
        try {
            IssVisiblePassTracker.formatResponse(json);
            throw new AssertionError(description);
        } catch (IOException | IllegalArgumentException expected) {
            check(description, true);
        }
    }

    private static void check(String description, boolean success) {
        if (!success) { throw new AssertionError(description); }
        passed++;
        System.out.println("PASS: " + description);
    }
}
