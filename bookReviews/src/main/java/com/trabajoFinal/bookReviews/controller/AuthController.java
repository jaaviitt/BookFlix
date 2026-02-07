package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(Usuario usuario) {
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);

        usuario.setRol("ROLE_ADMIN"); // ¡TRUCO! El primero que te crees, hazlo ADMIN para poder subir libros.
        usuarioRepository.save(usuario);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login"; // Necesitaremos crear este HTML o dejar que Spring use el suyo por defecto
    }
}