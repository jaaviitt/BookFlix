package com.trabajoFinal.bookReviews.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- PUNTUACIÓN (Estrellas) ---
    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    // --- COMENTARIO (Texto) ---
    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String comentario;

    // --- FECHA ---
    // Se guarda automáticamente el momento exacto en que se crea la reseña
    private LocalDateTime fecha = LocalDateTime.now();

    // --- RELACIÓN CON LIBRO ---
    // Muchas reseñas pertenecen a un solo libro.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    // --- RELACIÓN CON USUARIO ---
    // Muchas reseñas pertenecen a un solo usuario.
    // NOTA: Necesitamos tener creada la entidad Usuario para que esto funcione.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
