package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping("/")
    public String index(Model model, @RequestParam(value = "q", required = false) String query) {

        Map<String, List<Libro>> librosPorGenero = new LinkedHashMap<>();

        // CASO 1: BÚSQUEDA
        if (query != null && !query.isEmpty()) {
            List<Libro> resultados = libroRepository.findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrGenerosContainingIgnoreCase(query, query, query);

            if (!resultados.isEmpty()) {
                librosPorGenero.put("Resultados para: " + query, resultados);
            } else {
                librosPorGenero.put("No se encontraron resultados para: " + query, List.of());
            }

            // CASO 2: HOME NORMAL
        } else {
            List<String> generosDb = libroRepository.findDistinctGeneros();
            for (String genero : generosDb) {
                List<Libro> libros = libroRepository.findByGenerosContaining(genero);
                if (!libros.isEmpty()) {
                    librosPorGenero.put(genero, libros);
                }
            }
        }

        model.addAttribute("librosPorGenero", librosPorGenero);
        return "home";
    }
}