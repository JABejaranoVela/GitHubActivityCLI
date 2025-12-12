package io.github.jabejaranovela.githubactivity.service;

import io.github.jabejaranovela.githubactivity.model.GitHubEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GitHubEventsParser {

    public static List<GitHubEvent> parse(String json) {
        List<GitHubEvent> events = new ArrayList<>();

        String[] entries = json.split("\\},\\{");
        for (String entry : entries) {
            String type = extract(entry, "\"type\":\"", "\"");
            String repo = extract(entry, "\"repo\":\\{[^}]*\"name\":\"", "\"");
            String date = extract(entry, "\"created_at\":\"", "\"");
            String action = extract(entry, "\"action\":\"", "\"");
            String size = extract(entry, "\"size\":", ",");

            if (type == null || repo == null || date == null) continue;

            Instant createdAt = Instant.parse(date);
            int commitCount = size != null ? Integer.parseInt(size) : 0;

            events.add(new GitHubEvent(type, repo, createdAt, commitCount, action));
        }

        return events;
    }

    private static String extract(String source, String prefix, String end) {
        int start = source.indexOf(prefix);
        if (start == -1) return null;
        int begin = start + prefix.length();
        int finish = source.indexOf(end, begin);
        if (finish == -1) return null;
        return source.substring(begin, finish);
    }
}
