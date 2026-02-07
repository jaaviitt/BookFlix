package com.trabajoFinal.bookReviews.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleBooksService {

    private final String GOOGLE_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    public void rellenarDatosLibro(Libro libro) {
        try {
            // 1. Construimos la búsqueda inteligente
            String query = "intitle:" + libro.getTitulo().replace(" ", "+");

            // TRUCO: Si el usuario ha escrito el Autor, lo añadimos a la búsqueda para ser exactos
            if (libro.getAutor() != null && !libro.getAutor().isEmpty()) {
                query += "+inauthor:" + libro.getAutor().replace(" ", "+");
            }

            // Pedimos solo el primer resultado
            String url = GOOGLE_API_URL + query + "&maxResults=1";

            // 2. Hacemos la petición
            RestTemplate restTemplate = new RestTemplate();
            String respuestaJson = restTemplate.getForObject(url, String.class);

            // 3. Procesamos la respuesta
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(respuestaJson);

            if (root.path("items").isArray() && root.path("items").size() > 0) {
                JsonNode libroInfo = root.path("items").get(0).path("volumeInfo");

                // Solo rellenamos lo que esté vacío (para respetar lo que haya escrito el usuario)

                // AUTOR (Si estaba vacío, lo ponemos. Si el usuario lo puso para buscar, lo dejamos)
                if (libro.getAutor() == null || libro.getAutor().isEmpty()) {
                    if (libroInfo.path("authors").isArray()) {
                        libro.setAutor(libroInfo.path("authors").get(0).asText());
                    }
                }

                // GÉNERO
                if (libro.getGenero() == null || libro.getGenero().isEmpty()) {
                    if (libroInfo.path("categories").isArray()) {
                        libro.setGenero(libroInfo.path("categories").get(0).asText());
                    } else {
                        libro.setGenero("General");
                    }
                }

                // AÑO
                if (libro.getAnioPublicacion() == null) {
                    String fecha = libroInfo.path("publishedDate").asText();
                    if (fecha.length() >= 4) {
                        try {
                            libro.setAnioPublicacion(Integer.parseInt(fecha.substring(0, 4)));
                        } catch (NumberFormatException e) {
                            libro.setAnioPublicacion(2000); // Fallback por seguridad
                        }
                    }
                }

                // SINOPSIS
                if (libro.getSinopsis() == null || libro.getSinopsis().isEmpty()) {
                    String desc = libroInfo.path("description").asText();
                    if (desc.length() > 2000) desc = desc.substring(0, 1997) + "...";
                    libro.setSinopsis(desc);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}