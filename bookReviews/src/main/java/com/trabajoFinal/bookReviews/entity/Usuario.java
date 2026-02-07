package com.trabajoFinal.bookReviews.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    @Column(unique = true) // No queremos dos usuarios con el mismo nombre
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido") // Validación requerida por la rúbrica [cite: 107]
    @Column(unique = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // --- ROL (Para Spring Security) ---
    // Guardaremos "ROLE_USER" o "ROLE_ADMIN"
    private String rol;

    // --- RELACIÓN CON RESEÑAS ---
    // Un usuario escribe MUCHAS reseñas.
    // Al poner cascade = ALL, si borras al usuario, se borran sus reseñas (opcional, pero limpio).
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Resena> resenas = new ArrayList<>();
}
