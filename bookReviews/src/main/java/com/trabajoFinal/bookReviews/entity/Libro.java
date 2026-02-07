package com.trabajoFinal.bookReviews.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString; // Importante para evitar bucles infinitos con las relaciones
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    // --- LO QUE FALTABA (Datos extra) ---

    @NotNull(message = "El año es obligatorio")
    @Min(value = 1000, message = "Año no válido")
    private Integer anioPublicacion;

    @Size(max = 2000, message = "La sinopsis es demasiado larga") // Ampliamos a 2000 car.
    @Column(columnDefinition = "TEXT") // TEXT permite guardar textos muy largos en BBDD
    private String sinopsis;

    private String imagenUrl; // Portada (jpg)
    private String rutaPdf;   // Archivo del libro (pdf)

    // Fecha en la que subimos el libro a la web (útil para ordenar por "Novedades")
    private LocalDate fechaAlta = LocalDate.now();

    // --- LA RELACIÓN CRÍTICA (Libro -> Reseñas) ---
    // Un libro tiene MUCHAS reseñas.
    // "mappedBy = 'libro'" significa que en la clase Resena habrá un campo llamado 'libro'
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // Evita errores de recursividad al imprimir
    private List<Resena> resenas = new ArrayList<>();

    // Método de ayuda para añadir reseñas fácilmente
    public void agregarResena(Resena resena) {
        resenas.add(resena);
        resena.setLibro(this);
    }
}