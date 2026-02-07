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

        // 1. MAGIA: Llamamos a Google para rellenar los huecos
        googleBooksService.rellenarDatosLibro(libro);

        // 2. Procesamos el Archivo (PDF y Portada)
        if (!archivo.isEmpty()) {
            try {
                // Crear directorios si no existen
                new File(UPLOAD_DIR).mkdirs();
                new File(IMG_DIR).mkdirs();

                String nombreArchivo = archivo.getOriginalFilename();
                Path rutaArchivo = Paths.get(UPLOAD_DIR + nombreArchivo);
                Files.write(rutaArchivo, archivo.getBytes());

                // Guardamos la ruta relativa para que la web la encuentre
                libro.setRutaPdf("/uploads/" + nombreArchivo);

                // Generar portada del PDF
                try (PDDocument document = PDDocument.load(new File(UPLOAD_DIR + nombreArchivo))) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300);
                    String nombrePortada = nombreArchivo.replace(".pdf", ".jpg");
                    ImageIO.write(bim, "jpg", new File(IMG_DIR + nombrePortada));

                    libro.setImagenUrl("/img/" + nombrePortada);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        libroRepository.save(libro);
        return "redirect:/";
    }
}