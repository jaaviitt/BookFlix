package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LibroController {

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping("/libro/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        // Buscamos el libro por su ID. Si no existe, volvemos a la home (truco rápido)
        Libro libro = libroRepository.findById(id).orElse(null);

        if (libro == null) {
            return "redirect:/";
        }

        model.addAttribute("libro", libro);
        return "detalle"; // Carga el archivo detalle.html
    }
}