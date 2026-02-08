package com.trabajoFinal.bookReviews.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleBooksService {

    private final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?q=";

    public void rellenarDatosLibro(Libro libro) {
        try {
            if (libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()) {
                return;
            }

            // 1. Preparar URL
            String busqueda = libro.getTitulo().replace(" ", "+");
            if (libro.getAutor() != null && !libro.getAutor().isEmpty() && !libro.getAutor().equals("Autor Desconocido")) {
                busqueda += "+" + libro.getAutor().replace(" ", "+");
            }

            // AÑADIDO: Pedimos 'cover_i' (ID de portada) en la lista de campos
            String url = OPEN_LIBRARY_API + busqueda + "&fields=title,author_name,first_publish_year,subject,cover_i,first_sentence&limit=1";

            // 2. Conectar
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().add("User-Agent", "BookReviewsApp/1.0 (Student Project)");
                return execution.execute(request, body);
            });

            String respuestaJson = restTemplate.getForObject(url, String.class);

            // 3. Procesar JSON
            if (respuestaJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(respuestaJson);

                if (root.path("docs").isArray() && root.path("docs").size() > 0) {
                    JsonNode libroInfo = root.path("docs").get(0);

                    // --- AUTOR ---
                    if (libro.getAutor() == null || libro.getAutor().isEmpty() || libro.getAutor().equals("Autor Desconocido")) {
                        if (libroInfo.has("author_name") && libroInfo.path("author_name").isArray()) {
                            libro.setAutor(libroInfo.path("author_name").get(0).asText());
                        }
                    }

                    // --- AÑO ---
                    if (libro.getAnioPublicacion() == null || libro.getAnioPublicacion() == 2024) {
                        if (libroInfo.has("first_publish_year")) {
                            libro.setAnioPublicacion(libroInfo.get("first_publish_year").asInt());
                        }
                    }

                    // --- GÉNERO (Mejorado) ---
                    // Intentamos coger el primer género válido.
                    if (libroInfo.has("subject") && libroInfo.path("subject").isArray()) {
                        // Iteramos para buscar uno que no sea basura (opcional, aquí cogemos el 1º)
                        String generoEncontrado = libroInfo.path("subject").get(0).asText();

                        // Truco visual: Ponemos la primera letra en mayúscula
                        if (generoEncontrado != null && !generoEncontrado.isEmpty()) {
                            generoEncontrado = generoEncontrado.substring(0, 1).toUpperCase() + generoEncontrado.substring(1);
                            libro.setGenero(generoEncontrado);
                        }
                    }

                    // --- PORTADA (¡NUEVO!) ---
                    // OpenLibrary nos da un ID numérico (cover_i). Con eso construimos la URL.
                    if (libroInfo.has("cover_i")) {
                        int coverId = libroInfo.get("cover_i").asInt();
                        // URL oficial de portadas de OpenLibrary (L = Large/Grande)
                        String portadaUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
                        libro.setImagenUrl(portadaUrl);
                    }

                    // --- SINOPSIS ---
                    if (libro.getSinopsis() == null || libro.getSinopsis().contains("automáticamente")) {
                        if (libroInfo.has("first_sentence") && libroInfo.path("first_sentence").isArray()) {
                            String frase = libroInfo.path("first_sentence").get(0).asText();
                            libro.setSinopsis("Primera frase: " + frase + "...");
                        } else {
                            libro.setSinopsis("Sinopsis no disponible en OpenLibrary.");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error conectando con OpenLibrary: " + e.getMessage());
        }
    }
}