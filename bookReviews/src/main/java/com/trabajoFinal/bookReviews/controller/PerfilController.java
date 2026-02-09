package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Resena;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.ResenaRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @GetMapping("/perfil")
    public String verMiPerfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 1. Buscamos al usuario logueado en la BD
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (usuario != null) {
            // 2. Buscamos sus reseñas
            List<Resena> misResenas = resenaRepository.findByUsuario_Id(usuario.getId());

            // 3. Pasamos todo a la vista
            model.addAttribute("usuario", usuario);
            model.addAttribute("resenas", misResenas);
            model.addAttribute("totalResenas", misResenas.size());
        }

        return "perfil"; // Carga perfil.html
    }
}
