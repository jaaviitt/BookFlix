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
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // RECUPERADO

    @Autowired
    private GoogleBooksService googleBooksService;

    // RECUPERADO: Ruta a la raíz del proyecto para ver los archivos al instante
    private static final String UPLOAD_DIR = "uploads/";

    // --- 1. EL CENTRO DE COMANDOS (DASHBOARD) ---
    // ESTE MÉTODO FALTABA Y ES EL MÁS IMPORTANTE
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Libro> libros = libroRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();

        model.addAttribute("libros", libros);
        model.addAttribute("usuarios", usuarios);

        return "admin/dashboard";
    }

    @GetMapping("/nuevo-libro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("libro", new Libro());
        return "admin/formulario-libro";
    }

    @PostMapping("/guardar")
    public String guardarLibro(Libro libro, @RequestParam("archivoPdf") MultipartFile archivo) {

        // 1. Intentamos rellenar con Google/OpenLibrary
        googleBooksService.rellenarDatosLibro(libro);

        // --- PARACAÍDAS DE SEGURIDAD ---
        if (libro.getAutor() == null || libro.getAutor().trim().isEmpty()) {
            libro.setAutor("Autor Desconocido");
        }
        if (libro.getGenero() == null || libro.getGenero().trim().isEmpty()) {
            libro.setGenero("General");
        }
        if (libro.getAnioPublicacion() == null || libro.getAnioPublicacion() < 1000) {
            libro.setAnioPublicacion(2024);
        }
        if (libro.getSinopsis() == null || libro.getSinopsis().trim().isEmpty()) {
            libro.setSinopsis("Sin sinopsis disponible.");
        }

        // 2. Procesar PDF (CÓDIGO RECUPERADO)
        if (!archivo.isEmpty()) {
            try {
                // Crear directorio si no existe
                Path directorioImagenes = Paths.get(UPLOAD_DIR);
                if (!Files.exists(directorioImagenes)) {
                    Files.createDirectories(directorioImagenes);
                }

                // Guardar archivo
                byte[] bytes = archivo.getBytes();
                Path rutaCompleta = Paths.get(UPLOAD_DIR + archivo.getOriginalFilename());
                Files.write(rutaCompleta, bytes);

                // Asignar ruta al libro
                libro.setRutaPdf("/uploads/" + archivo.getOriginalFilename());

                // NOTA: Si tenías código para generar portada con PDFBox aquí,
                // el servicio de GoogleBooksService ya intenta buscar una portada online primero.

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Guardar
        libroRepository.save(libro);
        return "redirect:/admin/dashboard"; // Volvemos al dashboard en vez de al home
    }

    // --- 2. GESTIÓN DE USUARIOS (RECUPERADO) ---

    @PostMapping("/usuario/{id}/cambiar-rol")
    public String cambiarRolUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            if (usuario.getRoles().contains("ROLE_ADMIN")) {
                usuario.setRoles(Collections.singletonList("ROLE_USER"));
            } else {
                usuario.setRoles(Collections.singletonList("ROLE_ADMIN"));
            }
            usuarioRepository.save(usuario);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuario/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

    // --- 3. GESTIÓN DE LIBROS ---

    @GetMapping("/libro/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) return "redirect:/admin/dashboard";

        model.addAttribute("libro", libro);
        return "admin/editar_libro";
    }

    @PostMapping("/libro/{id}/actualizar")
    public String actualizarLibro(@PathVariable Long id, @ModelAttribute Libro libro) {
        Libro original = libroRepository.findById(id).orElse(null);
        if (original != null) {
            original.setTitulo(libro.getTitulo());
            original.setAutor(libro.getAutor());
            original.setGenero(libro.getGenero());
            original.setSinopsis(libro.getSinopsis());
            original.setImagenUrl(libro.getImagenUrl());

            libroRepository.save(original);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/libro/{id}/eliminar")
    public String eliminarLibro(@PathVariable Long id) {
        libroRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}