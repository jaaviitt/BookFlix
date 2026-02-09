package com.trabajoFinal.bookReviews.config;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import com.trabajoFinal.bookReviews.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GoogleBooksService googleBooksService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/";

    @Override
    public void run(String... args) throws Exception {

        // --- 1. CREAR ADMIN (Si no existe) ---
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@bookflix.com");
            // Le damos ambos roles para que pueda entrar al panel y ver la web normal
            admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

            usuarioRepository.save(admin);
            System.out.println("👑 Usuario ADMIN creado: admin / admin123");
        }

        // --- 2. ESCANEAR LIBROS ---
        System.out.println("📂 INICIANDO ESCANEO DE LIBROS...");
        File carpeta = new File(UPLOAD_DIR);

        // Si la carpeta uploads no existe, la crea
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            return;
        }

        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                // Filtramos solo archivos PDF
                if (archivo.isFile() && archivo.getName().toLowerCase().endsWith(".pdf")) {
                    procesarLibro(archivo);
                }
            }
        }
    }

    private void procesarLibro(File archivo) {
        String nombreArchivo = archivo.getName();
        // Ruta relativa para que el navegador pueda acceder a ella
        String rutaRelativa = "/uploads/" + nombreArchivo;

        // Si ya existe en la base de datos, lo saltamos para no duplicar
        if (libroRepository.existsByRutaPdf(rutaRelativa)) {
            return;
        }

        System.out.println("Nuevo libro detectado: " + nombreArchivo);

        Libro nuevoLibro = new Libro();
        nuevoLibro.setRutaPdf(rutaRelativa);

        // Intentar sacar título y autor del nombre del archivo
        String nombreSinExt = nombreArchivo.replace(".pdf", "");
        if (nombreSinExt.contains(" - ")) {
            String[] partes = nombreSinExt.split(" - ");
            nuevoLibro.setTitulo(partes[0].trim());
            if (partes.length > 1) nuevoLibro.setAutor(partes[1].trim());
        } else {
            nuevoLibro.setTitulo(nombreSinExt);
            nuevoLibro.setAutor("Autor Desconocido");
        }

        // Valores por defecto
        nuevoLibro.setGeneros(List.of("General"));
        nuevoLibro.setAnioPublicacion(2024);
        nuevoLibro.setSinopsis("Sinopsis pendiente...");

        // Llamar al servicio para buscar portada y datos extra en internet
        googleBooksService.rellenarDatosLibro(nuevoLibro);

        libroRepository.save(nuevoLibro);
    }
}