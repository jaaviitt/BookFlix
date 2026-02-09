package com.trabajoFinal.bookReviews.repository;

import com.trabajoFinal.bookReviews.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    // Buscar todas las reseñas de un libro (ya lo tenías)
    List<Resena> findByLibroId(Long libroId);

    // Buscar todas las reseñas hechas por un usuario específico
    List<Resena> findByUsuario_Id(Long usuarioId);
}
