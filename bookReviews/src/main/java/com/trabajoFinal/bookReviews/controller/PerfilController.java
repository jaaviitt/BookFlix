package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Resena;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.ResenaRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. VER PERFIL (GET) ---
    @GetMapping("/perfil")
    public String verMiPerfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 1. Buscamos al usuario logueado
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (usuario != null) {
            // 2. Buscamos sus reseñas
            List<Resena> misResenas = resenaRepository.findByUsuario_Id(usuario.getId());

            // 3. Pasamos todo a la vista
            model.addAttribute("usuario", usuario);
            model.addAttribute("resenas", misResenas);
            model.addAttribute("totalResenas", misResenas.size());
        }

        return "perfil";
    }

    // --- 2. GUARDAR CAMBIOS (POST) ---
    @PostMapping("/perfil/guardar")
    public String actualizarPerfil(@RequestParam String email,
                                   @RequestParam(required = false) String newPassword,
                                   @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (usuario != null) {
            // Actualizar Email
            usuario.setEmail(email);

            // Actualizar Contraseña (SOLO si el usuario escribió algo nuevo)
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(newPassword));
            }

            usuarioRepository.save(usuario);
        }

        return "redirect:/perfil?exito";
    }
}