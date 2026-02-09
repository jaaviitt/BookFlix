package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections; // <--- NECESARIO PARA LAS LISTAS

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute("usuario") Usuario usuario, BindingResult result, Model model) {

        // --- VALIDACIÓN 1: USUARIO ---
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe.");
            return "registro";
        }

        // --- VALIDACIÓN 2: EMAIL ---
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            model.addAttribute("error", "Ese correo electrónico ya está registrado en BookFlix.");
            return "registro"; // Volvemos al formulario con el error
        }

        // 3. Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 4. Asignar Rol de Usuario (CORREGIDO: Usando lista)
        usuario.setRoles(Collections.singletonList("ROLE_USER"));

        // 5. Guardar
        usuarioRepository.save(usuario);

        return "redirect:/login?registroExitoso";
    }
}