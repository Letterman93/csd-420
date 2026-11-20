/*
 * Author:      Zak Nizam
 * Date:        November 20, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 11.2 Programming Assignment - Additional Java Example
 *
 * Purpose: Request upcoming visible ISS passes and parse nested JSON with Jackson.
 * Developed from the N2YO REST API documentation, "Get visual passes":
 * https://www.n2yo.com/api/
 * The JSON tree parsing adapts FasterXML's Jackson Databind "Tree Model" example:
 * https://github.com/FasterXML/jackson-databind/tree/2.20#tree-model
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class IssVisiblePassTracker {
    // ISS, Kennedy Space Center (meters above sea level), ten days, 60 seconds.
    private static final String ENDPOINT = "https://api.n2yo.com/rest/v1/satellite/"
            + "visualpasses/25544/28.5729/-80.6490/0/10/60/";
    private static final ZoneId TIME_ZONE = ZoneId.of("America/New_York");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("MMMM d, uuuu 'at' h:mm:ss a z", Locale.US).withZone(TIME_ZONE);

    public static void main(String[] args) {
        // Keep the personal API key outside source control and the submission files.
        int result = run(System.getenv("N2YO_API_KEY"), System.out, System.err);
        if (result != 0) {
            System.exit(result);
        }
    }

    /** Perform one live request, displaying useful errors without exposing the key. */
    public static int run(String apiKey, PrintStream output, PrintStream errors) {
        if (apiKey == null || apiKey.isBlank()) {
            errors.println("Error: N2YO_API_KEY is not set. Set it in PowerShell, then run again.");
            return 1;
        }
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build()) {
            URI uri = URI.create(ENDPOINT + "&apiKey="
                    + URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8));
            output.println(formatResponse(fetchJson(client, uri)));
            return 0;
        } catch (JsonProcessingException failure) {
            errors.println("Error: N2YO returned invalid JSON.");
        } catch (IllegalArgumentException | DateTimeException failure) {
            errors.println("Error: " + failure.getMessage());
        } catch (HttpTimeoutException failure) {
            errors.println("Error: The N2YO request timed out. Try again later.");
        } catch (IOException failure) {
            // Network exceptions can contain the full URL, including its key.
            errors.println("Error: Could not retrieve N2YO data. Check the connection, API key, and usage limits.");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            errors.println("Error: The request was interrupted.");
        }
        return 1;
    }

    /** Send an HTTPS GET in normal use. Tests supply a local HTTP server. */
    public static String fetchJson(HttpClient client, URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(25))
                .header("Accept", "application/json").GET().build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            // Do not print the response body or request URL, which could contain a key.
            throw new IllegalArgumentException("N2YO returned HTTP " + response.statusCode()
                    + ". Check the API key and usage limits, or try again later.");
        }
        return response.body();
    }

    /** Parse the info object and passes array, then format the complete result. */
    public static String formatResponse(String json) throws JsonProcessingException {
        // ObjectMapper converts JSON text into a tree of JsonNode objects.
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        JsonNode root = mapper.readTree(json);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Expected a JSON object from N2YO.");
        }
        if (root.has("error")) {
            throw new IllegalArgumentException("N2YO reported an API error. Check the API key and usage limits.");
        }
        JsonNode info = root.path("info");
        JsonNode passes = root.path("passes");
        if (!info.isObject() || !passes.isArray()) {
            throw new IllegalArgumentException("The response must contain an info object and a passes array.");
        }
        String satellite = text(info, "satname");
        long satelliteId = integer(info, "satid");
        if (integer(info, "passescount") != passes.size()) {
            throw new IllegalArgumentException("The reported pass count does not match the passes array.");
        }
        StringBuilder result = new StringBuilder("ISS Visible Pass Tracker\n------------------------\n");
        result.append("Satellite: ").append(satellite).append('\n');
        result.append("NORAD ID: ").append(satelliteId).append('\n');
        result.append("Time zone: America/New_York\n");
        result.append("Visible passes found: ").append(passes.size()).append('\n');
        if (passes.isEmpty()) {
            result.append("\nNo visible passes meet these settings.\n");
        }
        int index = 1;
        // Each array element is another object containing one pass's values.
        for (JsonNode pass : passes) {
            long start = integer(pass, "startUTC");
            long end = integer(pass, "endUTC");
            double duration = number(pass, "duration");
            if (end < start || duration < 0) {
                throw new IllegalArgumentException("A pass contains an invalid time or duration.");
            }
            result.append("\nPass ").append(index++).append('\n');
            result.append("Start: ").append(formatTime(start)).append('\n');
            result.append("Start direction: ").append(text(pass, "startAzCompass")).append('\n');
            result.append(String.format(Locale.US, "Maximum elevation: %.1f degrees%n", number(pass, "maxEl")));
            result.append("Maximum direction: ").append(text(pass, "maxAzCompass")).append('\n');
            result.append("End: ").append(formatTime(end)).append('\n');
            result.append("End direction: ").append(text(pass, "endAzCompass")).append('\n');
            // Use the API's visible duration, which can differ from endUTC - startUTC.
            result.append(String.format(Locale.US, "Visible duration: %.1f minutes%n", duration / 60.0));
            JsonNode magnitude = pass.path("mag");
            // N2YO uses 100000 when brightness cannot be determined.
            if (magnitude.isMissingNode() || magnitude.isNull()
                    || (magnitude.isNumber() && magnitude.asDouble() == 100000)) {
                result.append("Magnitude: unavailable\n");
            } else {
                result.append(String.format(Locale.US, "Magnitude: %.1f%n", number(pass, "mag")));
            }
        }
        return result.toString();
    }

    /** Unix timestamps count seconds from the UTC epoch; ZoneId applies EST or EDT. */
    public static String formatTime(long unixSeconds) {
        return TIME_FORMAT.format(Instant.ofEpochSecond(unixSeconds));
    }

    private static String text(JsonNode object, String field) {
        JsonNode value = object.path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing or invalid text field: " + field);
        }
        return value.asText();
    }

    private static long integer(JsonNode object, String field) {
        JsonNode value = object.path(field);
        if (!value.isIntegralNumber() || !value.canConvertToLong()) {
            throw new IllegalArgumentException("Missing or invalid integer field: " + field);
        }
        return value.longValue();
    }

    private static double number(JsonNode object, String field) {
        JsonNode value = object.path(field);
        if (!value.isNumber() || !Double.isFinite(value.asDouble())) {
            throw new IllegalArgumentException("Missing or invalid numeric field: " + field);
        }
        return value.asDouble();
    }
}
