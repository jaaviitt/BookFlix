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

    // --- VER DETALLE DEL LIBRO ---
    @GetMapping("/libro/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        Libro libro = libroRepository.findById(id).orElse(null);

        if (libro == null) {
            return "redirect:/";
        }

        List<Resena> resenas = resenaRepository.findByLibroId(id);

        model.addAttribute("libro", libro);
        model.addAttribute("resenas", resenas);

        return "detalle";
    }

    // --- GUARDAR RESEÑA (VERSIÓN ROBUSTA) ---
    @PostMapping("/libro/{id}/resena")
    public String guardarResena(@PathVariable Long id,
                                @RequestParam String comentario,
                                @RequestParam int puntuacion,
                                @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("INTENTO DE RESEÑA RECIBIDO PARA LIBRO ID: " + id);

        // 1. Si no está logueado, fuera
        if (userDetails == null) {
            System.out.println("❌ Usuario no identificado.");
            return "redirect:/login";
        }

        // 2. Buscar Usuario (Por username O por email)
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .or(() -> usuarioRepository.findByEmail(userDetails.getUsername()))
                .orElse(null);

        // 3. Buscar Libro
        Libro libro = libroRepository.findById(id).orElse(null);

        // 4. Guardar
        if (libro != null && usuario != null) {
            // Si no tienes constructor, usamos los setters:
            Resena resena = new Resena();
            resena.setLibro(libro);
            resena.setUsuario(usuario);
            resena.setComentario(comentario);
            resena.setPuntuacion(puntuacion);
            // resena.setFecha(LocalDateTime.now());

            resenaRepository.save(resena);
            System.out.println("ÉXITO: Reseña guardada.");
        } else {
            System.out.println("ERROR: Usuario o Libro no encontrados.");
        }

        return "redirect:/libro/" + id;
    }
}