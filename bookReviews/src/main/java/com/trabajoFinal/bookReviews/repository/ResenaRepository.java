package com.trabajoFinal.bookReviews.repository;

import com.trabajoFinal.bookReviews.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByLibroId(Long libroId); // Para sacar todas las reseñas de un libro concreto
}
