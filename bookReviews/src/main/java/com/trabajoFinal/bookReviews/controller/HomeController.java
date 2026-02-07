package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller // Usamos @Controller (no @RestController) para devolver HTML
public class HomeController {

    @GetMapping("/")
    public String mostrarHome(Model model) {
        // 1. Creamos una lista falsa de libros para que el diseño no se vea vacío
        List<Libro> librosFalsos = new ArrayList<>();

        Libro l1 = new Libro();
        l1.setTitulo("El Quijote");
        l1.setGenero("Clásico");
        l1.setImagenUrl("https://edicionescatedra.com/imagenes/libros/grande/9788437606778-don-quijote-de-la-mancha-i.jpg");

        Libro l2 = new Libro();
        l2.setTitulo("1984");
        l2.setGenero("Ciencia Ficción");
        l2.setImagenUrl("https://m.media-amazon.com/images/I/71s1w2i7K+L._AC_UF1000,1000_QL80_.jpg");

        librosFalsos.add(l1);
        librosFalsos.add(l2);

        // 2. Pasamos la lista a la vista (HTML)
        model.addAttribute("libros", librosFalsos);

        // 3. Decimos qué archivo HTML mostrar (debe llamarse 'home.html' o 'index.html')
        return "home";
    }
}