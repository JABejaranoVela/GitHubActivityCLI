# ========== OUTPUT ==========
Output assets/github-activity-cli-demo.gif

# ========== REQUIREMENTS ==========
Require mvn
Require java
Require git

# ========== GLOBAL SETTINGS ==========
Set Shell zsh

Set FontFamily "JetBrainsMono Nerd Font"
Set FontSize 20
Set Width 1100
Set Height 550
Set Theme "Dracula"
Set Padding 18
Set Margin 30
Set MarginFill "#11111b"
Set WindowBar Colorful
Set BorderRadius 10
Set TypingSpeed 80ms
Set PlaybackSpeed 1.0

# ========== GO TO PROJECT (HIDDEN) ==========
Hide
Type 'cd "/home/tu_usuario/ruta/a/GitHubActivityCLI"'
Enter
Sleep 500ms

# Optional: warm up build to avoid slow first run/noisy downloads
Type "mvn -q -DskipTests package"
Enter
Sleep 1500ms

# Minimal prompt
Type 'PROMPT="> "'
Enter
Sleep 200ms

Type "clear"
Enter
Sleep 300ms
Show

# ========== DEMO ==========
# 1) Show usage (no args)
Type 'mvn -q exec:java -Dexec.mainClass="io.github.jabejaranovela.githubactivity.cli.Main"'
Enter
Sleep 3500ms

Type "clear"
Enter
Sleep 300ms

# 2) Run with a username
Type 'mvn -q exec:java -Dexec.mainClass="io.github.jabejaranovela.githubactivity.cli.Main" -Dexec.args="JABejaranoVela"'
Enter
Sleep 3500ms

