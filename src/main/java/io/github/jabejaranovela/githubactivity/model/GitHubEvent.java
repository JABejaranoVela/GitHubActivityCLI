package io.github.jabejaranovela.githubactivity.model;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GitHubEvent {
    private final String type;
    private final String repoName;
    private final Instant createdAt;
    private final int commitCount;
    private final String action;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GitHubEvent(String type, String repoName, Instant createdAt, int commitCount, String action) {
        this.type = type;
        this.repoName = repoName;
        this.createdAt = createdAt;
        this.commitCount = commitCount;
        this.action = action;
    }

    public String getEventDescription() {
        String date = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault()).format(FORMATTER);
        return switch (type) {
            case "PushEvent" -> date + " - Pushed " + commitCount + " commit(s) to " + repoName;
            case "IssuesEvent" -> date + " - " + action + " an issue in " + repoName;
            case "WatchEvent" -> date + " - Starred " + repoName;
            default -> date + " - Performed " + type + " in " + repoName;
        };
    }
}