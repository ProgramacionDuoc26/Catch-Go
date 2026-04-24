package com.progra.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
@Data // Lombok: genera getters, setters, toString automáticamente
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false, unique = true, length = 15)
    private String rut;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "tipo_usuario", nullable = false, length = 15)
    private String tipoUsuario; // 'TRABAJADOR' o 'EMPRESA'

    @Column(name = "estado_validacion", length = 20)
    private String estadoValidacion; 

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Este método se ejecuta automáticamente justo antes de guardar en la BD
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.estadoValidacion == null) {
            this.estadoValidacion = "PENDIENTE";
        }
    }
}