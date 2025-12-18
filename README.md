# GitHub Activity CLI

![GitHub Activity CLI demo](assets/github-activity-cli-demo.gif)

## Description

`GitHubActivityCLI` is a small command-line application written in **Java** that fetches and prints the most recent public activity of a GitHub user using the GitHub REST API.

This project is a solution for the **GitHub User Activity** challenge from roadmap.sh.

It can:

- Fetch recent events from the public endpoint: `https://api.github.com/users/<username>/events`
- Display the last **N** events (configurable in code)
- Optionally group consecutive identical events (same `type` + same `repo`) into a single output line

All HTTP calls are made using the **JDK built-in** `java.net.http.HttpClient` (no external fetch libraries).

* * *

## Requirements

- **Java 11+** (required for `HttpClient`)
- **Maven 3.6+**

* * *

## Installation

Clone the repository and build it with Maven:

```bash
git clone https://github.com/JABejaranoVela/GitHubActivityCLI
cd GitHubActivityCLI

mvn clean package
```

* * *

## Usage

Basic syntax:

```bash
mvn -q exec:java -Dexec.args="<github-username>"
```

Example:

```bash
mvn -q exec:java -Dexec.args="JABejaranoVela"
```

Example output (format may vary depending on current implementation):

```text
- Pushed 3 commits to JABejaranoVela/GitHubActivityCLI
- PublicEvent in JABejaranoVela/GitHubActivityCLI
- PushEvent x2 in JABejaranoVela/JABejaranoVela
```

* * *

## Configuration

### Number of events to display

The number of events printed is controlled by the `EVENTS_TO_DISPLAY` constant in `Main.java`:

```java
private static final int EVENTS_TO_DISPLAY = 10;
```

Change this value to show more or fewer events.

* * *

## Notes & limitations

- The GitHub events endpoint is **rate-limited**. If you hit the limit, the API may return `403` with rate-limit information.
- This project intentionally avoids external dependencies for HTTP fetching. JSON parsing is currently done with simple string extraction helpers to keep the implementation minimal.

* * *

## Resources

- Roadmap project: https://roadmap.sh/projects/github-user-activity
- GitHub Events API docs: https://docs.github.com/en/rest/activity/events

* * *

## License

This project is licensed under the **MIT License**.
You are free to use, modify and distribute it under the terms of that license.