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

    // --- PUNTUACIÓN ---
    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    // --- COMENTARIO ---
    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String comentario;

    // --- FECHA ---
    private LocalDateTime fecha = LocalDateTime.now();

    // --- RELACIÓN CON LIBRO ---
    @ManyToOne
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    // --- RELACIÓN CON USUARIO ---
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // --- CONSTRUCTORES ---

    // 1. Constructor vacío
    public Resena() {}

    // 2. Constructor lleno
    public Resena(Integer puntuacion, String comentario, Usuario usuario, Libro libro) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.usuario = usuario;
        this.libro = libro;
        this.fecha = LocalDateTime.now();
    }
}