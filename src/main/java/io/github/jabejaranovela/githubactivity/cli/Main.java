package io.github.jabejaranovela.githubactivity.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Main <nombre-de-usuario>");
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

            System.out.println("Código de respuesta: " + response.statusCode());
            if (response.statusCode() != 200) {
                // De momento, si no es 200 solo mostramos el body y salimos
                System.out.println("Respuesta no OK:");
                System.out.println(response.body());
                return;
            }

            String body = response.body();

            String json = body.trim();

            // Comprobamos que es un array JSON: empieza por [ y acaba por ]
            if (!json.startsWith("[") || !json.endsWith("]")) {
                System.out.println("Formato inesperado: no es un array JSON.");
                return;
            }

            // Quitamos los corchetes exteriores para trabajar con el contenido
            json = json.substring(1, json.length() - 1).trim();

            // Si está vacío, no hay eventos
            if (json.isEmpty()) {
                System.out.println("No hay eventos para este usuario.");
                return;
            }

            // Vamos a intentar quedarnos solo con el primer objeto del array.
            // Los objetos suelen estar separados por "},{".
            int separatorIndex = json.indexOf("},{");

            String firstEventJson;
            if (separatorIndex != -1) {
                // Hay más de un evento: cogemos desde el primer { hasta justo antes de "},"
                firstEventJson = json.substring(0, separatorIndex + 1); // incluye la primera }
            } else {
                // Solo hay un evento en el array
                firstEventJson = json;
            }

            // Nos aseguramos de que empieza por { y termina por }
            firstEventJson = firstEventJson.trim();
            if (!firstEventJson.startsWith("{")) {
                firstEventJson = "{" + firstEventJson;
            }
            if (!firstEventJson.endsWith("}")) {
                firstEventJson = firstEventJson + "}";
            }

            // 1. type
            String type = extractValue(firstEventJson, "\"type\":\"", "\"");
            if (type == null) {
                System.out.println("No se pudo encontrar el campo 'type' en el primer evento.");
                return;
            }

            // 2. repo.name
            String repoName = null;
            int repoIndex = firstEventJson.indexOf("\"repo\":");
            if (repoIndex != -1) {
                String repoPart = firstEventJson.substring(repoIndex);
                repoName = extractValue(repoPart, "\"name\":\"", "\"");
            }

            if (repoName == null) {
                System.out.println("No se pudo encontrar el campo 'repo.name' en el primer evento.");
                return;
            }

            // 3. Imprimir línea básica
            System.out.println("- " + type + " in " + repoName);

        } catch (IOException | InterruptedException e) {
            System.out.println("Error al conectar con la API: " + e.getMessage());
        }
    }

    private static String extractValue(String json, String prefix, String terminator) {
        int start = json.indexOf(prefix);
        if (start == -1) {
            return null;
        }
        start += prefix.length();
        int end = json.indexOf(terminator, start);
        if (end == -1) {
            return null;
        }
        return json.substring(start, end);
    }
}