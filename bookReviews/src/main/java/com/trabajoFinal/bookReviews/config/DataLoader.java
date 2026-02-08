package com.trabajoFinal.bookReviews.config;

import com.trabajoFinal.bookReviews.entity.Libro;
import com.trabajoFinal.bookReviews.repository.LibroRepository;
import com.trabajoFinal.bookReviews.service.GoogleBooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private GoogleBooksService googleBooksService;

    // Asegúrate de que esta carpeta existe en la raíz de tu proyecto
    private final String UPLOAD_DIR = "uploads/";

    @Override
    public void run(String... args) throws Exception {
        File carpeta = new File(UPLOAD_DIR);

        if (!carpeta.exists()) {
            System.out.println("⚠️ La carpeta 'uploads' no existe. Creándola...");
            carpeta.mkdirs();
            return;
        }

        System.out.println("📂 Escaneando carpeta 'uploads'...");
        File[] archivos = carpeta.listFiles();

        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && archivo.getName().toLowerCase().endsWith(".pdf")) {
                    procesarLibro(archivo);
                    System.out.println("⏳ Esperando 2 segundos...");
                    Thread.sleep(2000);
                }
            }
        }
        System.out.println("✅ Escaneo completado.");
    }

    private void procesarLibro(File archivo) {
        String nombreArchivo = archivo.getName();
        String rutaRelativa = "/uploads/" + nombreArchivo;

        // 1. Evitar duplicados
        if (libroRepository.existsByRutaPdf(rutaRelativa)) {
            return;
        }

        System.out.println("🔍 Procesando: " + nombreArchivo);

        Libro nuevoLibro = new Libro();
        nuevoLibro.setRutaPdf(rutaRelativa);

        // --- LÓGICA DE SEPARACIÓN (PARSING) ---
        // Quitamos la extensión .pdf
        String nombreSinExt = nombreArchivo.replace(".pdf", "");

        // Buscamos el separador " - " (espacio guion espacio)
        if (nombreSinExt.contains(" - ")) {
            String[] partes = nombreSinExt.split(" - ");

            // Parte 0: Título
            nuevoLibro.setTitulo(partes[0].trim());

            // Parte 1: Autor (si existe)
            if (partes.length > 1) {
                nuevoLibro.setAutor(partes[1].trim());
            } else {
                nuevoLibro.setAutor("Autor Desconocido");
            }
        } else {
            // Si el archivo no tiene guion (ej: "Dune.pdf"), usamos todo como título
            nuevoLibro.setTitulo(nombreSinExt);
            nuevoLibro.setAutor(""); // Lo dejamos vacío para que OpenLibrary lo busque
        }

        // Datos por defecto obligatorios
        nuevoLibro.setGenero("General");
        nuevoLibro.setAnioPublicacion(2024);
        nuevoLibro.setSinopsis("Libro importado automáticamente.");

        // 2. Pedimos a OpenLibrary que rellene lo que falta (Portada, Sinopsis, Año...)
        // Ahora buscará mucho mejor porque ya le damos el Autor.
        googleBooksService.rellenarDatosLibro(nuevoLibro);

        // 3. Guardar
        libroRepository.save(nuevoLibro);
        System.out.println("   --> Guardado: " + nuevoLibro.getTitulo() + " | Autor: " + nuevoLibro.getAutor());
    }
}
