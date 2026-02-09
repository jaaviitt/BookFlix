package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import com.trabajoFinal.bookReviews.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GoogleBooksService googleBooksService;

    // Ruta a la raíz del proyecto
    private static final String UPLOAD_DIR = "uploads/";

    // --- 1. DASHBOARD ---
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Libro> libros = libroRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();

        model.addAttribute("libros", libros);
        model.addAttribute("usuarios", usuarios);

        return "admin/dashboard";
    }

    // --- 2. FORMULARIO NUEVO LIBRO ---
    @GetMapping("/nuevo-libro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("libro", new Libro());
        return "admin/formulario-libro";
    }

    // --- 3. GUARDAR LIBRO (CON NUEVA LÓGICA DE GÉNEROS) ---
    @PostMapping("/guardar")
    public String guardarLibro(Libro libro,
                               @RequestParam("archivoPdf") MultipartFile archivo,
                               @RequestParam(value = "generoTexto", required = false) String generoTexto) {

        // A. PROCESAR GÉNEROS (Texto -> Lista)
        // Recibimos "Terror, Suspense" y lo convertimos a lista
        if (generoTexto != null && !generoTexto.trim().isEmpty()) {
            List<String> lista = Arrays.stream(generoTexto.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            libro.setGeneros(lista);
        }

        // B. LLAMAR AL SERVICIO (OpenLibrary)
        googleBooksService.rellenarDatosLibro(libro);

        // C. PARACAÍDAS DE SEGURIDAD (Valores por defecto)
        if (libro.getAutor() == null || libro.getAutor().trim().isEmpty()) {
            libro.setAutor("Autor Desconocido");
        }
        // Corrección para la lista de géneros
        if (libro.getGeneros() == null || libro.getGeneros().isEmpty()) {
            libro.setGeneros(List.of("General"));
        }
        if (libro.getAnioPublicacion() == null || libro.getAnioPublicacion() < 1000) {
            libro.setAnioPublicacion(2024);
        }
        if (libro.getSinopsis() == null || libro.getSinopsis().trim().isEmpty()) {
            libro.setSinopsis("Sin sinopsis disponible.");
        }

        // D. PROCESAR PDF (TU CÓDIGO ORIGINAL)
        if (!archivo.isEmpty()) {
            try {
                Path directorioImagenes = Paths.get(UPLOAD_DIR);
                if (!Files.exists(directorioImagenes)) {
                    Files.createDirectories(directorioImagenes);
                }

                byte[] bytes = archivo.getBytes();
                Path rutaCompleta = Paths.get(UPLOAD_DIR + archivo.getOriginalFilename());
                Files.write(rutaCompleta, bytes);

                libro.setRutaPdf("/uploads/" + archivo.getOriginalFilename());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        libroRepository.save(libro);
        return "redirect:/admin/dashboard";
    }

    // --- 4. EDITAR LIBRO ---
    @GetMapping("/libro/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) return "redirect:/admin/dashboard";

        model.addAttribute("libro", libro);
        return "admin/editar_libro";
    }

    @PostMapping("/libro/{id}/actualizar")
    public String actualizarLibro(@PathVariable Long id,
                                  @ModelAttribute Libro libro,
                                  @RequestParam(value = "generoTexto", required = false) String generoTexto) {

        Libro original = libroRepository.findById(id).orElse(null);
        if (original != null) {
            original.setTitulo(libro.getTitulo());
            original.setAutor(libro.getAutor());
            original.setSinopsis(libro.getSinopsis());
            original.setImagenUrl(libro.getImagenUrl());
            // No actualizamos el PDF aquí para no borrarlo si no suben uno nuevo

            // Actualizar Géneros (Texto -> Lista)
            if (generoTexto != null && !generoTexto.trim().isEmpty()) {
                List<String> lista = Arrays.stream(generoTexto.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                original.setGeneros(lista);
            }

            libroRepository.save(original);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/libro/{id}/eliminar")
    public String eliminarLibro(@PathVariable Long id) {
        libroRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- 5. GESTIÓN DE USUARIOS (CON PROTECCIÓN SUPREMA) ---

    @PostMapping("/usuario/{id}/cambiar-rol")
    public String cambiarRolUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario != null) {
            // 🛡️ PROTECCIÓN SUPREMA: Si es el admin principal, NO SE TOCA.
            if ("admin".equalsIgnoreCase(usuario.getUsername())) {
                System.out.println("⛔ INTENTO DE MODIFICAR AL SUPER ADMIN BLOQUEADO.");
                return "redirect:/admin/dashboard?error=superadmin";
            }

            // Lógica de cambio de rol
            List<String> rolesActuales = usuario.getRoles();

            if (rolesActuales.contains("ROLE_ADMIN")) {
                // ANTES (Fallaba): usuario.setRoles(List.of("ROLE_USER"));
                // AHORA (Correcto): Usamos ArrayList mutable
                usuario.setRoles(new ArrayList<>(List.of("ROLE_USER")));
            } else {
                // ANTES (Fallaba): usuario.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));
                // AHORA (Correcto): Usamos ArrayList mutable
                usuario.setRoles(new ArrayList<>(List.of("ROLE_ADMIN", "ROLE_USER")));
            }

            usuarioRepository.save(usuario);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        // 🛡️ PROTECCIÓN SUPREMA
        if (usuario != null) {
            if ("admin".equalsIgnoreCase(usuario.getUsername())) {
                System.out.println("⛔ INTENTO DE ELIMINAR AL SUPER ADMIN BLOQUEADO.");
                return "redirect:/admin/dashboard?error=superadmin";
            }
            usuarioRepository.deleteById(id);
        }
        return "redirect:/admin/dashboard";
    }
}