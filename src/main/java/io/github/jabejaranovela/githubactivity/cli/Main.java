package io.github.jabejaranovela.githubactivity.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int EVENTS_TO_DISPLAY = 10;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <github-username>");
            return;
        }

        String username = args[0];
        String url = "https://api.github.com/users/" + username + "/events";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Java-GitHubActivityCLI")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            if (response.statusCode() != 200) {
                // For now, if it's not 200 we print the body and exit
                System.out.println("Non-OK response:");
                System.out.println(response.body());
                return;
            }

            String responseBody = response.body().trim();

            // Validate this is a JSON array: starts with [ and ends with ]
            if (!responseBody.startsWith("[") || !responseBody.endsWith("]")) {
                System.out.println("Unexpected format: response is not a JSON array.");
                return;
            }

            // Remove outer brackets to work with the array content
            String eventsArrayContent = responseBody.substring(1, responseBody.length() - 1).trim();

            // If empty, there are no events
            if (eventsArrayContent.isEmpty()) {
                System.out.println("No events found for this user.");
                return;
            }

            // Extract and print up to N events
            List<String> events = extractEvents(eventsArrayContent, EVENTS_TO_DISPLAY);
            for (String eventJson : events) {
                String line = formatEvent(eventJson);
                if (line != null) {
                    System.out.println("- " + line);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // good practice: restore interrupt flag
            System.out.println("Request interrupted: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to call GitHub API: " + e.getMessage());
        }
    }

    private static String extractValue(String json, String prefix, String terminator) {
        int startIndex = json.indexOf(prefix);
        if (startIndex == -1) {
            return null;
        }

        startIndex += prefix.length();
        int endIndex = json.indexOf(terminator, startIndex);
        if (endIndex == -1) {
            return null;
        }

        return json.substring(startIndex, endIndex);
    }

    private static List<String> extractEvents(String jsonArrayContent, int maxEvents) {
        List<String> events = new ArrayList<>();

        int cursor = 0;
        int extractedCount = 0;

        while (extractedCount < maxEvents) {
            int separatorIndex = jsonArrayContent.indexOf("},{", cursor);

            if (separatorIndex == -1) {
                // Last (or only) remaining event
                String lastEventJson = jsonArrayContent.substring(cursor).trim();
                if (!lastEventJson.isEmpty()) {
                    events.add(normalizeJsonObject(lastEventJson));
                }
                break;
            }

            String eventJson = jsonArrayContent.substring(cursor, separatorIndex + 1).trim(); // includes the closing }
            events.add(normalizeJsonObject(eventJson));

            cursor = separatorIndex + 3; // skip "},{"
            extractedCount++;
        }

        return events;
    }

    private static String formatEvent(String eventJson) {
        if (eventJson == null) {
            return null;
        }

        String normalizedEventJson = normalizeJsonObject(eventJson);

        // 1) type
        String type = extractValue(normalizedEventJson, "\"type\":\"", "\"");
        if (type == null) {
            return null;
        }

        // 2) repo.name
        int repoIndex = normalizedEventJson.indexOf("\"repo\":");
        if (repoIndex == -1) {
            return null;
        }

        String repoSectionJson = normalizedEventJson.substring(repoIndex);
        String repoName = extractValue(repoSectionJson, "\"name\":\"", "\"");
        if (repoName == null) {
            return null;
        }

        return type + " in " + repoName;
    }

    private static String normalizeJsonObject(String jsonObjectText) {
        String normalized = jsonObjectText.trim();

        // Safety: ensure braces
        if (!normalized.startsWith("{")) {
            normalized = "{" + normalized;
        }
        if (!normalized.endsWith("}")) {
            normalized = normalized + "}";
        }
        return normalized;
    }
}
