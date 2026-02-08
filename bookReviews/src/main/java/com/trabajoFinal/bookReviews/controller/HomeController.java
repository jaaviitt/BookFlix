package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping("/")
    public String index(Model model) {
        // 1. Obtenemos todos los géneros únicos disponibles
        List<String> generos = libroRepository.findDistinctGeneros();

        // 2. Creamos un mapa para guardar: "Ciencia Ficción" -> [Libro1, Libro2...]
        // Usamos LinkedHashMap para mantener el orden
        Map<String, List<Libro>> librosPorGenero = new LinkedHashMap<>();

        for (String genero : generos) {
            // Buscamos los libros de este género
            List<Libro> libros = libroRepository.findByGenero(genero);

            // Solo añadimos la categoría si tiene libros
            if (!libros.isEmpty()) {
                librosPorGenero.put(genero, libros);
            }
        }

        // 3. Pasamos el mapa a la vista
        model.addAttribute("librosPorGenero", librosPorGenero);

        return "home";
    }
}