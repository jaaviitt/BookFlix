package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.entity.Resena;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.repository.ResenaRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LibroController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ESTE ES EL MÉTODO QUE TE FALTA O FALLA
    @GetMapping("/libro/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        // 1. Buscamos el libro por ID
        Libro libro = libroRepository.findById(id).orElse(null);

        // 2. Si no existe (o el ID es null), volvemos a la home para evitar errores
        if (libro == null) {
            return "redirect:/";
        }

        // 3. Cargamos las reseñas
        List<Resena> resenas = resenaRepository.findByLibroId(id);

        // 4. Pasamos los datos a la vista
        model.addAttribute("libro", libro);
        model.addAttribute("resenas", resenas);

        return "detalle"; // Esto carga el archivo detalle.html
    }

    // GUARDAR RESEÑA
    @PostMapping("/libro/{id}/resena")
    public String guardarResena(@PathVariable Long id,
                                @RequestParam String comentario,
                                @RequestParam int puntuacion,
                                @AuthenticationPrincipal UserDetails userDetails) {

        Libro libro = libroRepository.findById(id).orElse(null);
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (libro != null && usuario != null) {
            Resena resena = new Resena();
            resena.setLibro(libro);
            resena.setUsuario(usuario);
            resena.setComentario(comentario);
            resena.setPuntuacion(puntuacion);

            resenaRepository.save(resena);
        }

        return "redirect:/libro/" + id;
    }
}