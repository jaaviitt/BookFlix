package com.trabajoFinal.bookReviews.controller;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.service.GoogleBooksService; // <--- IMPORTANTE
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private GoogleBooksService googleBooksService; // <--- INYECCIÓN DEL SERVICIO

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/"; // Ruta ajustada
    private static final String IMG_DIR = "src/main/resources/static/img/";

    @GetMapping("/nuevo-libro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("libro", new Libro());
        return "admin/formulario-libro";
    }

    @PostMapping("/guardar")
    public String guardarLibro(Libro libro, @RequestParam("archivoPdf") MultipartFile archivo) {

        // 1. Intentamos que Google rellene los datos
        googleBooksService.rellenarDatosLibro(libro);

        // --- CORRECCIÓN DE SEGURIDAD ---
        // Si Google ha fallado o no ha encontrado nada, ponemos valores por defecto
        // para que la base de datos no se queje.

        if (libro.getAutor() == null || libro.getAutor().isEmpty()) {
            libro.setAutor("Autor Desconocido");
        }

        if (libro.getGenero() == null || libro.getGenero().isEmpty()) {
            libro.setGenero("General");
        }

        if (libro.getAnioPublicacion() == null) {
            libro.setAnioPublicacion(2024); // Año actual o desconocido
        }

        if (libro.getSinopsis() == null || libro.getSinopsis().isEmpty()) {
            libro.setSinopsis("Sin sinopsis disponible.");
        }
        // -------------------------------

        // 2. Procesamos el Archivo PDF (Esto déjalo igual que lo tenías)
        if (!archivo.isEmpty()) {
            try {
                // ... (tu código de subida de archivos) ...
                // Asegúrate de que las rutas son las correctas:
                // new File("src/main/resources/static/uploads/").mkdirs();

                // NOTA: Para este ejemplo resumo tu código anterior aquí:
                String nombreArchivo = archivo.getOriginalFilename();
                // ... guardar archivo ...
                libro.setRutaPdf("/uploads/" + nombreArchivo);
                // ... generar portada ...
                // libro.setImagenUrl(...)

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. Guardamos (Ahora ya no fallará porque no hay nulos)
        libroRepository.save(libro);
        return "redirect:/";
    }
}