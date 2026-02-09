package com.trabajoFinal.bookReviews.repository;

import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    // Método para detectar duplicados
    boolean existsByRutaPdf(String rutaPdf);

    // Obtener una lista de todos los géneros únicos que existen (sin repetir)
    @Query("SELECT DISTINCT l.genero FROM Libro l")
    List<String> findDistinctGeneros();

    // Buscar libros por un género concreto
    List<Libro> findByGenero(String genero);

    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrGeneroContainingIgnoreCase(String titulo, String autor, String genero);
}