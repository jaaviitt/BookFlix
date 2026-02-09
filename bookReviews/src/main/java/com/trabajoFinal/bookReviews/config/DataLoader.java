package com.trabajoFinal.bookReviews.config;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import com.trabajoFinal.bookReviews.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate; // <--- 1. IMPORT NUEVO
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

    @Autowired
    private JdbcTemplate jdbcTemplate; // <--- 2. HERRAMIENTA PARA SALVAR DATOS

    private final String UPLOAD_DIR = "uploads/";

    @Override
    public void run(String... args) throws Exception {

        // --- 0. MIGRACIÓN DE DATOS (SALVAR GÉNEROS ANTIGUOS) ---
        // Este bloque intenta coger lo que escribiste a mano en la columna antigua
        // y moverlo a la nueva lista.
        try {
            // Comprobamos si la columna vieja "genero" (singular) todavía existe y tiene datos
            // La consulta copia: ID del libro + Texto del género -> Tabla nueva
            String sqlMigracion = "INSERT INTO libro_generos (libro_id, genero) " +
                    "SELECT id, genero FROM libros " +
                    "WHERE genero IS NOT NULL " +
                    "AND id NOT IN (SELECT libro_id FROM libro_generos)";

            int filasRecuperadas = jdbcTemplate.update(sqlMigracion);

            if (filasRecuperadas > 0) {
                System.out.println("✅ MIGRACIÓN ÉXITOSA: Se han recuperado los géneros de " + filasRecuperadas + " libros.");
            }
        } catch (Exception e) {
            // Si esto falla es porque ya se migró o la columna vieja ya no existe. No pasa nada.
            System.out.println("ℹ️ No se requirió migración o la estructura ya está actualizada.");
        }
        // --------------------------------------------------------


        // --- 1. CREAR ADMIN (Si no existe) ---
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@bookflix.com");

            admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));

            usuarioRepository.save(admin);
            System.out.println("👑 Usuario ADMIN creado: admin / admin123");
        }

        // --- 2. ESCANEAR LIBROS ---
        System.out.println("📂 INICIANDO ESCANEO DE LIBROS...");
        File carpeta = new File(UPLOAD_DIR);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
            return;
        }

        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && archivo.getName().toLowerCase().endsWith(".pdf")) {
                    procesarLibro(archivo);
                    // Pausa para Google
                    Thread.sleep(2000);
                }
            }
        }
    }

    private void procesarLibro(File archivo) {
        String nombreArchivo = archivo.getName();
        String rutaRelativa = "/uploads/" + nombreArchivo;

        if (libroRepository.existsByRutaPdf(rutaRelativa)) {
            return;
        }

        System.out.println("🔍 Procesando: " + nombreArchivo);

        Libro nuevoLibro = new Libro();
        nuevoLibro.setRutaPdf(rutaRelativa);

        String nombreSinExt = nombreArchivo.replace(".pdf", "");
        if (nombreSinExt.contains(" - ")) {
            String[] partes = nombreSinExt.split(" - ");
            nuevoLibro.setTitulo(partes[0].trim());
            if (partes.length > 1) nuevoLibro.setAutor(partes[1].trim());
        } else {
            nuevoLibro.setTitulo(nombreSinExt);
            nuevoLibro.setAutor("");
        }

        // Valores por defecto (AHORA USANDO LISTA)
        nuevoLibro.setGeneros(List.of("General")); // <--- CORREGIDO PARA USAR LISTA
        nuevoLibro.setAnioPublicacion(2024);
        nuevoLibro.setSinopsis("Sinopsis pendiente...");

        googleBooksService.rellenarDatosLibro(nuevoLibro);

        libroRepository.save(nuevoLibro);
        System.out.println("   --> Guardado: " + nuevoLibro.getTitulo());
    }
}