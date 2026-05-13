package com.catchandgo.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "habilidades_usuario")
public class HabilidadUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "habilidad_id")
    private Habilidad habilidad;

    private Integer puntos = 0;

    @Column(name = "trabajos_aplicados")
    private Integer trabajosAplicados = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Habilidad getHabilidad() { return habilidad; }
    public void setHabilidad(Habilidad habilidad) { this.habilidad = habilidad; }
    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }
    public Integer getTrabajosAplicados() { return trabajosAplicados; }
    public void setTrabajosAplicados(Integer trabajosAplicados) { this.trabajosAplicados = trabajosAplicados; }
}
