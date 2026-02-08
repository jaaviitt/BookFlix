package com.trabajoFinal.bookReviews.repository;

import com.trabajoFinal.bookReviews.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    // Método para detectar duplicados
    boolean existsByRutaPdf(String rutaPdf);
}