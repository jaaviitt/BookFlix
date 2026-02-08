package com.trabajoFinal.bookReviews.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleBooksService {

    // URL de Google (Mejor calidad de datos)
    private final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    // URL de Open Library (Plan B)
    private final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?q=";

    public void rellenarDatosLibro(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()) {
            return;
        }

        boolean exitoGoogle = consultarGoogleBooks(libro);

        // Si Google falla o no devuelve datos esenciales, probamos con Open Library
        if (!exitoGoogle) {
            System.out.println("⚠️ Google falló con '" + libro.getTitulo() + "'. Intentando OpenLibrary...");
            consultarOpenLibrary(libro);
        }
    }

    // --- MÉTODO 1: GOOGLE BOOKS (La opción preferida) ---
    private boolean consultarGoogleBooks(Libro libro) {
        try {
            String query = "intitle:" + libro.getTitulo().replace(" ", "+");
            if (libro.getAutor() != null && !libro.getAutor().isEmpty() && !libro.getAutor().contains("Desconocido")) {
                query += "+inauthor:" + libro.getAutor().replace(" ", "+");
            }

            String url = GOOGLE_API_URL + query + "&maxResults=1&langRestrict=es"; // Priorizamos español

            RestTemplate restTemplate = new RestTemplate();
            String respuestaJson = restTemplate.getForObject(url, String.class);

            if (respuestaJson == null) return false;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(respuestaJson);

            if (root.path("totalItems").asInt() > 0 && root.path("items").isArray()) {
                JsonNode info = root.path("items").get(0).path("volumeInfo");

                // AUTOR
                if (libro.getAutor() == null || libro.getAutor().isEmpty() || libro.getAutor().contains("Desconocido")) {
                    if (info.path("authors").isArray()) {
                        libro.setAutor(info.path("authors").get(0).asText());
                    }
                }

                // GÉNERO (Google usa "categories")
                if (info.path("categories").isArray()) {
                    libro.setGenero(info.path("categories").get(0).asText());
                }

                // AÑO
                if (info.has("publishedDate")) {
                    String fecha = info.get("publishedDate").asText();
                    if (fecha.length() >= 4) {
                        libro.setAnioPublicacion(Integer.parseInt(fecha.substring(0, 4)));
                    }
                }

                // SINOPSIS (Google suele tener buenas descripciones)
                if (info.has("description")) {
                    String desc = info.get("description").asText();
                    // Limpiamos etiquetas HTML si vienen
                    desc = desc.replaceAll("<[^>]*>", "");
                    if (desc.length() > 2000) desc = desc.substring(0, 1997) + "...";
                    libro.setSinopsis(desc);
                }

                // PORTADA (Google Thumbnail)
                if (info.path("imageLinks").has("thumbnail")) {
                    String img = info.path("imageLinks").get("thumbnail").asText();
                    // Google devuelve http, cambiamos a https para evitar avisos
                    libro.setImagenUrl(img.replace("http://", "https://"));
                }

                return true; // ¡Éxito!
            }
        } catch (Exception e) {
            // Si falla (503, error de red...), devolvemos false para que salte al Plan B
            return false;
        }
        return false;
    }

    // --- MÉTODO 2: OPEN LIBRARY (El Plan B) ---
    private void consultarOpenLibrary(Libro libro) {
        try {
            String query = libro.getTitulo().replace(" ", "+");
            if (libro.getAutor() != null && !libro.getAutor().isEmpty() && !libro.getAutor().contains("Desconocido")) {
                query += "+" + libro.getAutor().replace(" ", "+");
            }

            String url = OPEN_LIBRARY_API + query + "&fields=title,author_name,first_publish_year,subject,cover_i,first_sentence&limit=1";

            RestTemplate restTemplate = new RestTemplate();
            // User-Agent para evitar bloqueos
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

                    // Rellenar solo lo que Google no haya encontrado (o si Google falló totalmente)
                    if (libro.getAutor() == null || libro.getAutor().contains("Desconocido")) {
                        if (doc.path("author_name").isArray()) libro.setAutor(doc.path("author_name").get(0).asText());
                    }

                    if (libro.getGenero() == null || libro.getGenero().equals("General")) {
                        if (doc.path("subject").isArray()) libro.setGenero(doc.path("subject").get(0).asText());
                    }

                    if (libro.getAnioPublicacion() == null || libro.getAnioPublicacion() == 2024) {
                        if (doc.has("first_publish_year")) libro.setAnioPublicacion(doc.get("first_publish_year").asInt());
                    }

                    if (libro.getImagenUrl() == null && doc.has("cover_i")) {
                        libro.setImagenUrl("https://covers.openlibrary.org/b/id/" + doc.get("cover_i") + "-L.jpg");
                    }

                    // Si llegamos aquí y no hay sinopsis, ponemos un mensaje mejor
                    if (libro.getSinopsis() == null || libro.getSinopsis().contains("automáticamente")) {
                        if (doc.path("first_sentence").isArray()) {
                            libro.setSinopsis("Primera frase: " + doc.path("first_sentence").get(0).asText());
                        } else {
                            libro.setSinopsis("No hay sinopsis disponible, pero es una gran lectura.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Ambos servicios fallaron para: " + libro.getTitulo());
        }
    }
}