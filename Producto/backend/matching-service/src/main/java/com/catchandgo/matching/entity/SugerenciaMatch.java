package com.catchandgo.matching.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "match_suggestions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_match_suggestion_offer_worker",
        columnNames = {"job_offer_id", "worker_profile_id"}
    )
)
public class SugerenciaMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_offer_id", nullable = false)
    private Long ofertaTrabajoId;

    @Column(name = "worker_profile_id", nullable = false)
    private Long trabajadorPerfilId;

    @Column(name = "worker_name", nullable = false)
    private String nombreTrabajador;

    @Column(name = "total_score", nullable = false)
    private Double puntajeTotal;

    @Column(name = "skills_score", nullable = false)
    private Double puntajeHabilidades;

    @Column(name = "experience_score", nullable = false)
    private Double puntajeExperiencia;

    @Column(name = "distance_score", nullable = false)
    private Double puntajeDistancia;

    @Column(name = "availability_score", nullable = false)
    private Double puntajeDisponibilidad;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOfertaTrabajoId() { return ofertaTrabajoId; }
    public void setOfertaTrabajoId(Long ofertaTrabajoId) { this.ofertaTrabajoId = ofertaTrabajoId; }
    public Long getTrabajadorPerfilId() { return trabajadorPerfilId; }
    public void setTrabajadorPerfilId(Long trabajadorPerfilId) { this.trabajadorPerfilId = trabajadorPerfilId; }
    public String getNombreTrabajador() { return nombreTrabajador; }
    public void setNombreTrabajador(String nombreTrabajador) { this.nombreTrabajador = nombreTrabajador; }
    public Double getPuntajeTotal() { return puntajeTotal; }
    public void setPuntajeTotal(Double puntajeTotal) { this.puntajeTotal = puntajeTotal; }
    public Double getPuntajeHabilidades() { return puntajeHabilidades; }
    public void setPuntajeHabilidades(Double puntajeHabilidades) { this.puntajeHabilidades = puntajeHabilidades; }
    public Double getPuntajeExperiencia() { return puntajeExperiencia; }
    public void setPuntajeExperiencia(Double puntajeExperiencia) { this.puntajeExperiencia = puntajeExperiencia; }
    public Double getPuntajeDistancia() { return puntajeDistancia; }
    public void setPuntajeDistancia(Double puntajeDistancia) { this.puntajeDistancia = puntajeDistancia; }
    public Double getPuntajeDisponibilidad() { return puntajeDisponibilidad; }
    public void setPuntajeDisponibilidad(Double puntajeDisponibilidad) { this.puntajeDisponibilidad = puntajeDisponibilidad; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
