package com.trabajoFinal.bookReviews.repository;

import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // 1. Buscar libros que contengan ese género en su lista
    List<Libro> findByGenerosContaining(String genero);

    // 2. Buscar libros por generos distintos
    @Query("SELECT DISTINCT g FROM Libro l JOIN l.generos g")
    List<String> findDistinctGeneros();

    // 3. Buscador Global
    List<Libro> findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrGenerosContainingIgnoreCase(String titulo, String autor, String genero);

    // 4. Para el escáner (evitar duplicados)
    boolean existsByRutaPdf(String rutaPdf);
}