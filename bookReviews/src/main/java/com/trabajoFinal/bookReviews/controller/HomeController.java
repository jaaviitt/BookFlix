package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private LibroRepository libroRepository; // Inyectamos el almacén de libros

    @GetMapping("/")
    public String index(Model model) {
        // ANTES: Tenías aquí una lista falsa creada a mano (new Libro...)

        // AHORA: Pedimos los libros reales a la base de datos
        List<Libro> libros = libroRepository.findAll();

        model.addAttribute("libros", libros);
        return "home"; // Tu archivo home.html
    }
}