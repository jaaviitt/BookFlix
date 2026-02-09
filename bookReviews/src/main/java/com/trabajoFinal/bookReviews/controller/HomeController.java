package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // <--- IMPORTANTE

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private LibroRepository libroRepository;

    // AQUI ESTABA EL ERROR: Faltaba añadir "@RequestParam..." dentro de los paréntesis
    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "q", required = false) String query) {

        // MAPA para guardar los libros: "Nombre Categoria" -> [Lista de Libros]
        Map<String, List<Libro>> librosPorGenero = new LinkedHashMap<>();

        // CASO 1: EL USUARIO HA BUSCADO ALGO
        if (query != null && !query.isEmpty()) {
            // Buscamos en Título, Autor o Género
            List<Libro> resultados = libroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrGeneroContainingIgnoreCase(query, query, query);

            // Guardamos los resultados en una "categoría" especial
            if (!resultados.isEmpty()) {
                librosPorGenero.put("Resultados para: " + query, resultados);
            } else {
                librosPorGenero.put("No se encontraron resultados para: " + query, List.of());
            }

            // CASO 2: NO HAY BÚSQUEDA
        } else {
            List<String> generos = libroRepository.findDistinctGeneros();

            for (String genero : generos) {
                // Buscamos los libros de este género
                List<Libro> libros = libroRepository.findByGenero(genero);

                // Solo añadimos la categoría si tiene libros
                if (!libros.isEmpty()) {
                    librosPorGenero.put(genero, libros);
                }
            }
        }

        // Pasamos el mapa a la vista
        model.addAttribute("librosPorGenero", librosPorGenero);

        return "home";
    }
}