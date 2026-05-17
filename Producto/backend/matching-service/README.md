# Servicio de Match

Motor de matching automatico entre ofertas de empresa y trabajadores candidatos.

## Formula de score

El score final se calcula en escala 0 a 100:

```text
score = habilidades * 0.40
      + experiencia * 0.20
      + distancia/tiempo * 0.20
      + disponibilidad * 0.20
```

## Ejecutar matching

```http
POST /matching/ejecutar
```

```json
{
  "ofertaTrabajo": {
    "id": 10,
    "titulo": "Operario de bodega",
    "empresaId": 20,
    "habilidadesRequeridas": ["bodega", "carga"],
    "experienciaRequeridaMeses": 12,
    "latitud": -33.4489,
    "longitud": -70.6693,
    "disponibilidadRequerida": [
      { "diaSemana": "lunes", "horaInicio": "08:00", "horaFin": "16:00" }
    ]
  },
  "trabajadores": [
    {
      "id": 30,
      "nombre": "Camila Rojas",
      "habilidades": ["bodega", "carga", "aseo"],
      "experienciaMeses": 18,
      "latitud": -33.45,
      "longitud": -70.66,
      "disponibilidad": [
        { "diaSemana": "lunes", "horaInicio": "07:00", "horaFin": "17:00" }
      ]
    }
  ]
}
```

El servicio elimina las sugerencias previas de la oferta, recalcula todos los candidatos y persiste las sugerencias ordenadas por puntaje.

## Consultar sugerencias

```http
GET /matching/ofertas-trabajo/{ofertaTrabajoId}/sugerencias
```

La respuesta incluye `puntajeTotal`, `puntajeHabilidades`, `puntajeExperiencia`, `puntajeDistancia` y `puntajeDisponibilidad` para explicar por que un trabajador fue recomendado.
