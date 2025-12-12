package io.github.jabejaranovela.githubactivity.service;

import io.github.jabejaranovela.githubactivity.api.GitHubApiClient;
import io.github.jabejaranovela.githubactivity.model.GitHubEvent;

import java.util.List;

public class GitHubService {

    private final GitHubApiClient apiClient = new GitHubApiClient();

    public void showUserActivity(String username) {
        List<GitHubEvent> events = apiClient.fetchUserEvents(username);

        if (events.isEmpty()) {
            System.out.println("No se encontró actividad reciente para el usuario.");
            return;
        }

        for (GitHubEvent event : events) {
            System.out.println("- " + event.getEventDescription());
        }
    }
}
