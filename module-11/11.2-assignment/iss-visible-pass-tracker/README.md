# ISS Visible Pass Tracker

Zak Nizam | CSD 420 | Module 11.2 | November 20, 2026

This program requests visible ISS passes from N2YO and uses Jackson's `ObjectMapper` and `JsonNode` to read the JSON response. It searches ten days from Kennedy Space Center (28.5729, -80.6490, altitude 0 meters), keeping passes visible for at least 60 seconds. Times appear in `America/New_York`, with EST or EDT applied automatically.

## Build and run

Use JDK 21 or newer and Maven, with the `java` and `mvn` commands on PATH. Open this project folder in VS Code and use its terminal.

Compile the program:

```powershell
mvn clean compile
```

Get a free REST API key by registering or signing in at [N2YO](https://www.n2yo.com/login/). Open your profile and scroll to the API-key generation button, as described in the [API documentation](https://www.n2yo.com/api/).

Set the key for the current terminal without typing it into source code or command history:

```powershell
$key = Read-Host "N2YO API key" -AsSecureString
$env:N2YO_API_KEY = [Net.NetworkCredential]::new("", $key).Password
Remove-Variable key
mvn -q compile exec:java
```

The program reports a missing key, failed request, invalid response, or JSON parsing error. If no passes meet the search settings, it displays a no-results message.

## Example output

`live-output.txt` contains output from a successful request to N2YO on November 20, 2026. Since the API returns current pass predictions, the results will change when the program is run again.

## Jackson JARs

`../jackson-2.20.1-jars.zip` includes all three runtime libraries. Maven downloads the same dependencies automatically.

| JAR | Download |
| --- | --- |
| jackson-databind-2.20.1.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.20.1/jackson-databind-2.20.1.jar) |
| jackson-core-2.20.1.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.20.1/jackson-core-2.20.1.jar) |
| jackson-annotations-2.20.jar | [Maven Central](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.20/jackson-annotations-2.20.jar) |

Jackson 2.x matches the `com.fasterxml.jackson` imports used in this example.

## References

- N2YO. (n.d.). *N2YO.com REST API v1: Get visual passes.* https://www.n2yo.com/api/ (accessed November 20, 2026). This was used as a reference for the endpoint and response fields.
- FasterXML. (n.d.). *Jackson Databind: Tree Model (2.20 branch).* https://github.com/FasterXML/jackson-databind/tree/2.20#tree-model (accessed November 20, 2026). This was used as a reference for `ObjectMapper`, `readTree()`, and working with `JsonNode` values.
