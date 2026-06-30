# Plan de Calidad y Pruebas Automatizadas: Android (Kotlin)
## Proyecto: Catch & Go — App Móvil Nativa

Este documento detalla la estrategia de aseguramiento de calidad, análisis estático y pruebas automatizadas recomendada para la aplicación móvil nativa desarrollada en **Kotlin** y **Jetpack Compose** de Catch & Go.

---

## 1. Análisis Estático de Código (SAST)
Para mantener la calidad del código Kotlin, consistencia en el estilo y evitar malas prácticas en Jetpack Compose, se implementará:

### A. Detekt (Analizador Estático para Kotlin)
`detekt` es el estándar de la industria para el análisis estático en Kotlin. Detecta complejidad cognitiva, funciones muy largas, y code smells.
* **Configuración en [build.gradle.kts](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/android-app/build.gradle.kts):**
  ```kotlin
  plugins {
      id("io.gitlab.arturbosch.detekt") version "1.23.6"
  }
  ```
* **Comando para ejecutar el escaneo:**
  ```bash
  ./gradlew detekt
  ```

### B. Android Lint
Herramienta nativa de Android Studio que analiza problemas de rendimiento, accesibilidad (WCAG AA), y uso de APIs obsoletas.
* **Comando para ejecutar el escaneo:**
  ```bash
  ./gradlew lintDebug
  ```

---

## 2. Pruebas Unitarias (Unit Testing)
Se centran en probar la lógica de negocio aislada: **ViewModels**, **Casos de Uso (Use Cases)** y **Repositorios** usando dobles de prueba (mocks).

### Stack Recomendado:
1. **JUnit 5:** Framework de ejecución de pruebas.
2. **MockK:** Librería nativa de Kotlin para mockear clases y objetos.
3. **kotlinx-coroutines-test:** Para probar código asíncrono y coroutines de Kotlin en ViewModels.

### Ejemplo Práctico: Test de un ViewModel (Login)
Cuando se implemente el flujo de Login en la Fase 3, el ViewModel tendrá la lógica para validar las credenciales.

#### Aplicación de Complejidad Ciclomática en Kotlin:
```kotlin
class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) { // <-- bifurcación 1
            _uiState.value = Error("Campos obligatorios")
            return
        }
        // ... llamada de red asíncrona ...
    }
}
```
* **Cálculo:** 1 condicional (`isBlank()`) ➔ Complejidad ciclomática $M = 2$.
* **Pruebas JUnit necesarias:** 
  1. Login fallido por campos en blanco.
  2. Login exitoso con credenciales correctas.

#### Código del Test Unitario (`LoginViewModelTest.kt`):
```kotlin
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class LoginViewModelTest {

    private val repository = mockk<AuthRepository>()
    private val viewModel = LoginViewModel(repository)

    @Test
    fun `login con campos vacios retorna error de validacion`() = runTest {
        viewModel.login("", "")
        val state = viewModel.uiState.value
        assertTrue(state is UIState.Error)
        assertEquals("Campos obligatorios", (state as UIState.Error).message)
    }
}
```

---

## 3. Pruebas de Interfaz de Usuario (UI / Integration Testing)
En Jetpack Compose, las pruebas de interfaz verifican que los elementos visuales se rendericen correctamente y que respondan a la interacción del usuario de acuerdo a las pautas de diseño (áreas de click $\ge$ 48dp, feedback visual, etc.).

### Stack Recomendado:
* **Compose Test Rule:** Librería oficial de Jetpack para interactuar de forma aislada con componentes de Compose (Semantics).

### Ejemplo de Prueba de UI (`ButtonTest.kt`):
Prueba para verificar que nuestro botón del Design System responde correctamente al hacer clic.
```kotlin
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verificar_boton_ejecuta_click() {
        var clicked = false

        // 1. Montar el componente en el entorno de pruebas
        composeTestRule.setContent {
            CatchGoButton(
                text = "Confirmar",
                onClick = { clicked = true }
            )
        }

        // 2. Buscar el botón por su texto y simular click
        composeTestRule.onNodeWithText("Confirmar").performClick()

        // 3. Verificar la acción
        assert(clicked)
    }
}
```
* **Comando para ejecutar pruebas de UI:**
  ```bash
  ./gradlew connectedAndroidTest
  ```

---

## 4. Cobertura de Código en Android
Para medir y visualizar qué tanto código Kotlin está cubierto por las pruebas unitarias, se utilizará:

* **JetBrains Kover:** La herramienta oficial de JetBrains diseñada específicamente para Kotlin y proyectos Multiplatform/Android. Genera reportes HTML interactivos similares a JaCoCo pero optimizados para coroutines y funciones inline de Kotlin.
* **Comando para generar el reporte de cobertura:**
  ```bash
  ./gradlew koverHtmlReport
  ```
  *(El reporte HTML se creará en `app/build/reports/kover/html/index.html`)*.
