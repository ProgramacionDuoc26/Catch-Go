package com.catchandgo.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "habilidades")
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private CategoriaHabilidad categoria;

    private Boolean predeterminada = false;

    @Column(name = "creador_usuario_id")
    private Long creadorUsuarioId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public CategoriaHabilidad getCategoria() { return categoria; }
    public void setCategoria(CategoriaHabilidad categoria) { this.categoria = categoria; }
    public Boolean getPredeterminada() { return predeterminada; }
    public void setPredeterminada(Boolean predeterminada) { this.predeterminada = predeterminada; }
    public Long getCreadorUsuarioId() { return creadorUsuarioId; }
    public void setCreadorUsuarioId(Long creadorUsuarioId) { this.creadorUsuarioId = creadorUsuarioId; }
}
