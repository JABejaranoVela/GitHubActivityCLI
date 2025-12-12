package io.github.jabejaranovela.githubactivity.api;

import io.github.jabejaranovela.githubactivity.model.GitHubEvent;
import io.github.jabejaranovela.githubactivity.service.GitHubEventsParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

public class GitHubApiClient {

    private static final String BASE_URL = "https://api.github.com/users/";

    public List<GitHubEvent> fetchUserEvents(String username) {
        String url = BASE_URL + username + "/events";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Java-GitHubActivityCLI")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new RuntimeException("Usuario no encontrado.");
            } else if (response.statusCode() >= 400) {
                throw new RuntimeException("Error HTTP: " + response.statusCode());
            }

            return GitHubEventsParser.parse(response.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Fallo al conectar con la API de GitHub", e);
        }
    }
}