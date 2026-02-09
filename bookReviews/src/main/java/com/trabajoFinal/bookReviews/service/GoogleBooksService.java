package com.trabajoFinal.bookReviews.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class GoogleBooksService {

    private final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?q=";

    public void rellenarDatosLibro(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()) {
            return;
        }

        System.out.println("🌍 Consultando OpenLibrary para: " + libro.getTitulo());
        consultarOpenLibrary(libro);
    }

    private void consultarOpenLibrary(Libro libro) {
        try {
            // Construimos la URL de búsqueda
            String query = libro.getTitulo().replace(" ", "+");
            if (libro.getAutor() != null && !libro.getAutor().isEmpty() && !libro.getAutor().contains("Desconocido")) {
                query += "+" + libro.getAutor().replace(" ", "+");
            }

            // Pedimos solo los campos que nos interesan
            String url = OPEN_LIBRARY_API + query + "&fields=title,author_name,first_publish_year,subject,cover_i,first_sentence&limit=1";

            // Truco para evitar que OpenLibrary nos bloquee (fingimos ser un navegador)
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                return execution.execute(request, body);
            });

            String respuestaJson = restTemplate.getForObject(url, String.class);

            if (respuestaJson != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(respuestaJson);

                if (root.path("docs").isArray() && root.path("docs").size() > 0) {
                    JsonNode doc = root.path("docs").get(0);

                    // 1. AUTOR
                    if (libro.getAutor() == null || libro.getAutor().contains("Desconocido")) {
                        if (doc.path("author_name").isArray()) {
                            libro.setAutor(doc.path("author_name").get(0).asText());
                        }
                    }

                    // 2. GÉNEROS (Adaptado a tu nueva Lista)
                    // Si el género actual es "General" o está vacío, intentamos coger el de internet
                    if (libro.getGeneros() == null || libro.getGeneros().isEmpty() ||
                            (libro.getGeneros().size() == 1 && libro.getGeneros().get(0).equals("General"))) {

                        if (doc.path("subject").isArray()) {
                            // OpenLibrary devuelve MUCHOS géneros, cogemos solo el primero para no saturar
                            String generoPrincipal = doc.path("subject").get(0).asText();
                            libro.setGeneros(Collections.singletonList(generoPrincipal));
                        }
                    }

                    // 3. AÑO
                    if (libro.getAnioPublicacion() == null || libro.getAnioPublicacion() == 2024) {
                        if (doc.has("first_publish_year")) {
                            libro.setAnioPublicacion(doc.get("first_publish_year").asInt());
                        }
                    }

                    // 4. PORTADA
                    if (libro.getImagenUrl() == null && doc.has("cover_i")) {
                        libro.setImagenUrl("https://covers.openlibrary.org/b/id/" + doc.get("cover_i") + "-L.jpg");
                    }

                    // 5. SINOPSIS
                    if (libro.getSinopsis() == null || libro.getSinopsis().contains("pendiente")) {
                        if (doc.path("first_sentence").isArray()) {
                            libro.setSinopsis("Primera frase: " + doc.path("first_sentence").get(0).asText());
                        } else {
                            // OpenLibrary a veces no tiene sinopsis completa, así que dejamos el aviso
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