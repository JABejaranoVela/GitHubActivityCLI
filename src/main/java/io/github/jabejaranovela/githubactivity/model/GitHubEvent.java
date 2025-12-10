package io.github.jabejaranovela.githubactivity.model;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Clase base que representa un evento genérico de GitHub.
 * Contiene campos comunes y un método abstracto para describir el evento.
 */
public abstract class GitHubEvent {
    private String repoName;
    private Instant createdAt; // Momento en que ocurrió el evento (horaUTC)
    // Formato de fecha legible (día/mes/año horas:minutos)
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GitHubEvent(String repoName, Instant createdAt) {
        this.repoName = repoName;
        this.createdAt = createdAt;
    }

    /**
     * Devuelve una descripción legible del evento, incluyendo fecha y detalles.
     * Este método será implementado por cada tipo específico de evento.
     */
    public abstract String getEventDescription();

    public String getRepoName() {
        return repoName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Formatea la fecha/hora del evento en una representación legible para mostrar al usuario.
     */
    protected String formatDateTime() {
    // Convertimos Instant (UTC) a hora local del sistema para más legibilidad
        LocalDateTime localTime = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault());
        return localTime.format(DATE_FORMAT);
    }
}