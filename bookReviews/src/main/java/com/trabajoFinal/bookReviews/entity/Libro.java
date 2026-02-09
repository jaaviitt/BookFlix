package com.trabajoFinal.bookReviews.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String autor;
    private Integer anioPublicacion;

    @Column(columnDefinition = "TEXT")
    private String sinopsis;

    private String imagenUrl;
    private String rutaPdf;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "libro_generos", joinColumns = @JoinColumn(name = "libro_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    // --- GETTERS Y SETTERS MANUALES ---

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    // Método extra para añadir un solo género fácilmente
    public void addGenero(String genero) {
        if (this.generos == null) {
            this.generos = new ArrayList<>();
        }
        this.generos.add(genero);
    }
}