# ISS Visible Pass Tracker

Zak Nizam | CSD 420 | Module 11.2 | November 20, 2026

This program requests visible ISS passes from N2YO and uses Jackson's `ObjectMapper` and `JsonNode` to read the JSON response. It searches ten days from Kennedy Space Center (28.5729, -80.6490, altitude 0 meters), keeping passes visible for at least 60 seconds. Times appear in `America/New_York`, with EST or EDT applied automatically.

## Build and run

Use JDK 21 or newer and Maven, with the `java` and `mvn` commands on PATH. Open this project folder in VS Code and use its terminal.

Compile and run the offline checks:

```powershell
mvn clean test
```

Get a free REST API key by registering or signing in at [N2YO](https://www.n2yo.com/login/). Open your profile and scroll to the API-key generation button, as described in the [API documentation](https://www.n2yo.com/api/).

Set the key for the current terminal without typing it into source code or command history:

```powershell
$key = Read-Host "N2YO API key" -AsSecureString
$env:N2YO_API_KEY = [Net.NetworkCredential]::new("", $key).Password
Remove-Variable key
mvn -q compile exec:java
```

The application always makes a live request when run. It does not print the key or request URL. It reports a missing key, failed request, non-200 response, invalid JSON, or unexpected response fields. A valid response with no passes displays a normal no-results message. Each run uses one API transaction.

## Example output and checks

`../live-output.txt` contains the output captured from a successful live request on November 20, 2026. Predictions will change when the program is run again.

`sample-output.txt` shows parser output from N2YO's **published historical March 2018 example**. The historical response makes it possible to test the parser without making another live API request. Regenerate it with:

```powershell
mvn -q test-compile exec:java "-Dexec.mainClass=IssVisiblePassTrackerChecks" "-Dexec.classpathScope=test" "-Dexec.args=--sample"
```

`mvn test` runs the check program automatically. It uses the published JSON response and a local HTTP server; no API key or live N2YO calls are needed. Test-only modifications exercise missing fields, bad data, and unavailable magnitudes. A failed check fails the build. A missing test-resource file produces an explicit error.

## Jackson JARs

`../jackson-2.20.1-jars.zip` includes all three runtime libraries. Maven downloads the same dependencies automatically. The original JARs retain their included license and notice files.

| JAR | Download |
| --- | --- |
| jackson-databind-2.20.1.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.20.1/jackson-databind-2.20.1.jar) |
| jackson-core-2.20.1.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.20.1/jackson-core-2.20.1.jar) |
| jackson-annotations-2.20.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar) |

Jackson 2.x matches the `com.fasterxml.jackson` imports used in this example.

## References

- N2YO. (n.d.). *N2YO.com REST API v1: Get visual passes.* https://www.n2yo.com/api/ (accessed November 20, 2026). The endpoint, response fields, magnitude sentinel, and historical test response come from this documentation.
- FasterXML. (n.d.). *Jackson Databind: Tree Model (2.20 branch).* https://github.com/FasterXML/jackson-databind/tree/2.20#tree-model (accessed November 20, 2026). This was used as a reference for `ObjectMapper`, `readTree()`, and working with `JsonNode` values.
