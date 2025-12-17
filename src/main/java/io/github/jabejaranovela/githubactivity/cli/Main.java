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

            // Extract up to N events
            List<String> events = extractEvents(eventsArrayContent, EVENTS_TO_DISPLAY);

            // Group and format
            List<String> lines = groupAndFormatEvents(events);
            for (String line : lines) {
                System.out.println("- " + line);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // good practice: restore interrupt flag
            System.out.println("Request interrupted: " + e.getMessage());
        }
        catch (IOException e) {
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

    private static List<String> groupAndFormatEvents(List<String> eventsJson) {
        List<String> lines = new ArrayList<>();
        if (eventsJson == null || eventsJson.isEmpty()) {
            return lines;
        }

        String currentType = null;
        String currentRepo = null;

        int currentOccurrences = 0;
        int currentCommitTotal = 0; // only meaningful for PushEvent

        for (String eventJson : eventsJson) {
            String normalizedEventJson = normalizeJsonObject(eventJson);

            String type = extractValue(normalizedEventJson, "\"type\":\"", "\"");
            String repoName = extractRepoName(normalizedEventJson);

            // If we can't parse the minimum required fields, skip this event
            if (type == null || repoName == null) {
                continue;
            }

            boolean sameGroup = type.equals(currentType) && repoName.equals(currentRepo);

            if (!sameGroup) {
                // Flush previous group
                if (currentType != null) {
                    lines.add(formatGroupedLine(currentType, currentRepo, currentOccurrences, currentCommitTotal));
                }

                // Start new group
                currentType = type;
                currentRepo = repoName;
                currentOccurrences = 0;
                currentCommitTotal = 0;
            }

            currentOccurrences++;

            // Optional: for PushEvent, accumulate commits using payload.size when present
            if ("PushEvent".equals(type)) {
                Integer pushSize = extractPushCommitCount(normalizedEventJson);
                if (pushSize != null) {
                    currentCommitTotal += pushSize;
                }
            }
        }

        // Flush last group
        if (currentType != null) {
            lines.add(formatGroupedLine(currentType, currentRepo, currentOccurrences, currentCommitTotal));
        }

        return lines;
    }

    private static String extractRepoName(String normalizedEventJson) {
        int repoIndex = normalizedEventJson.indexOf("\"repo\":");
        if (repoIndex == -1) {
            return null;
        }
        String repoSectionJson = normalizedEventJson.substring(repoIndex);
        return extractValue(repoSectionJson, "\"name\":\"", "\"");
    }

    private static Integer extractPushCommitCount(String normalizedEventJson) {
        int payloadIndex = normalizedEventJson.indexOf("\"payload\":");
        if (payloadIndex == -1) {
            return null;
        }
        String payloadSectionJson = normalizedEventJson.substring(payloadIndex);
        return extractIntValue(payloadSectionJson, "\"size\":");
    }

    private static Integer extractIntValue(String json, String prefix) {
        int startIndex = json.indexOf(prefix);
        if (startIndex == -1) {
            return null;
        }

        startIndex += prefix.length();

        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        int endIndex = startIndex;
        while (endIndex < json.length() && Character.isDigit(json.charAt(endIndex))) {
            endIndex++;
        }

        if (endIndex == startIndex) {
            return null;
        }

        try {
            return Integer.parseInt(json.substring(startIndex, endIndex));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String formatGroupedLine(String type, String repoName, int occurrences, int commitTotal) {
        if ("PushEvent".equals(type)) {
            // If payload.size was available, use it; otherwise fallback to number of events
            int commits = (commitTotal > 0) ? commitTotal : occurrences;
            String commitWord = (commits == 1) ? "commit" : "commits";
            return "Pushed " + commits + " " + commitWord + " to " + repoName;
        }

        // Generic grouping for any other event type
        if (occurrences == 1) {
            return type + " in " + repoName;
        }
        return type + " x" + occurrences + " in " + repoName;
    }
}
