# HABILIDAD.md - Ingeniero UI/UX Transaccional (Catch and Go)

## Resumen
Esta habilidad guía la creación de interfaces front-end de grado industrial, transaccionales y enfocadas 100% en la **usabilidad, la accesibilidad y la eficiencia**. Rechaza estéticas recargadas, maximalistas o puramente decorativas.
El objetivo es generar interfaces que transmitan confianza, solidez corporativa y que permitan al usuario completar su tarea (publicar un empleo o postularse) en la menor cantidad de clics y tiempo posible.

## Pensamiento de Diseño (Antes de programar)
Antes de generar código, comprende el contexto operativo y comprométete con esta dirección:
* **Propósito:** Es una plataforma para resolver urgencias laborales temporales. El usuario no tiene tiempo para explorar; necesita acciones claras inmediatas.
* **Tono:** Profesional, utilitario, limpio, confiable y accesible. El diseño debe transmitir la seriedad de un contrato laboral.
* **Restricciones:** Enfoque Mobile-First absoluto. Tiempos de carga mínimos (optimización para redes móviles inestables). Alta accesibilidad (contraste WCAG AA mínimo) para una demografía amplia de trabajadores.
* **Diferenciación:** La "fricción cero". El diseño debe guiar el ojo instantáneamente hacia el "Call to Action" (Llamado a la acción) principal.

## Directrices de Estética y Desarrollo Frontend

### 1. Tipografía (Legibilidad extrema)
* **Regla:** Utiliza fuentes del sistema o tipografías sans-serif altamente legibles y neutras (como Inter, Roboto, San Francisco o Segoe UI). 
* **Prohibición:** NUNCA uses fuentes decorativas, script, o con remates (serif) para las interfaces de la aplicación. 
* **Jerarquía:** Establece una jerarquía clara usando pesos (bold, semibold, regular) y tamaños estandarizados.

### 2. Color y Tema (Confianza y Claridad)
* Usa paletas sobrias y corporativas. Fondos blancos o grises muy claros (`#F9FAFB`) para reducir la fatiga visual.
* Aplica el color primario (azul corporativo) estrictamente para los elementos interactivos principales (botones, enlaces activos).
* Usa colores semánticos de forma estándar: Verde para éxito/matching, Rojo para errores/alertas, Amarillo/Naranja para esperas/calificaciones de estrellas.

### 3. Composición Espacial y Grillas
* Respeta estrictamente las grillas de 8pt o 4pt. 
* **Áreas táctiles:** Asegúrate de que todos los botones y áreas clickeables tengan un tamaño mínimo de 48x48px (estándar de accesibilidad móvil).
* Usa abundante espacio en blanco (espacio negativo) para separar bloques de información, en lugar de usar múltiples líneas o bordes duros.

### 4. Movimiento (Micro-interacciones funcionales)
* **Regla:** Cero animaciones puramente decorativas o vinculadas al scroll que bloqueen la lectura.
* **Permitido:** Solo utiliza animaciones cortas (menos de 200ms) para retroalimentación directa: un botón que se presiona, un esqueleto de carga (skeleton loader) mientras llegan los datos de Mapbox, o un modal de éxito al finalizar una postulación.

### 5. Estructura de Componentes (React/Next.js y Android/Kotlin)
* Genera código de producción modular. Extrae elementos repetitivos (tarjetas de oferta, botones, avatares con ranking) en componentes reutilizables.
* Prioriza el rendimiento. En React/Next.js usa los componentes adecuados para evitar renderizados innecesarios.

**IMPORTANTE:** NUNCA generes estéticas caóticas, brutalistas, asimétricas o "artísticas". La elegancia de este proyecto reside en su simplicidad operativa y en plasmar una interfaz en la que un trabajador de cualquier nivel tecnológico pueda postular a un turno con un solo toque.