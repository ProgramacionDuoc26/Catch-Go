# Guía Técnica: Cálculo del Número de Pruebas Unitarias (JUnit)
## Método de Complejidad Ciclomática (McCabe) aplicado a Catch & Go

Esta guía explica la fundamentación científica y la fórmula matemática utilizada para calcular **cuántas pruebas unitarias (JUnit) se deben realizar** en una clase de Java según sus líneas de código y estructuras de decisión.

---

## 1. La Teoría: Complejidad Ciclomática de McCabe
El número de pruebas unitarias mínimas necesarias para cubrir todas las rutas posibles de ejecución en una función o clase se calcula mediante la **Complejidad Ciclomática**, un modelo de ingeniería de software desarrollado por Thomas McCabe en 1976.

La complejidad ciclomática ($M$) mide el número de caminos linealmente independientes a través del código fuente de un programa.

### La Fórmula General del Grafo de Flujo:
$$M = E - N + 2P$$

Donde:
* **$E$ (Edges / Aristas):** El número de conexiones o transiciones de una línea de código a otra en el flujo.
* **$N$ (Nodes / Nodos):** Las sentencias o bloques de código secuenciales.
* **$P$ (Predicates / Componentes conexos):** Para una sola función, $P = 1$.

### La Regla Práctica de los Puntos de Decisión:
Para que sea extremadamente fácil de calcular sin dibujar un grafo, se utiliza la fórmula simplificada:

$$M = D + 1$$

Donde **$D$** es el número de **puntos de decisión** en el código.
Un punto de decisión es cualquier estructura que bifurca el flujo del programa:
* `if`
* `else if`
* Operador ternario (`? :`)
* Cláusulas `case` en un `switch`
* Bucles (`for`, `while`, `do-while`)
* Operadores lógicos en condiciones condicionales (`&&`, `||`)
* Capturas de excepciones (`catch`)

---

## 2. Aplicación Práctica: Caso Real `UserAccountService.java`
Analicemos el código del servicio [UserAccountService.java](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/backend/services/auth-service/src/main/java/com/catchandgo/auth/service/UserAccountService.java) de nuestro backend para calcular su complejidad ciclomática método por método:

### Método 1: `register`
```java
public AuthResponseDto register(RegisterRequestDto dto) {
    if (repository.findByEmail(dto.email()).isPresent()) { // <--- 1 punto de decisión (if)
        throw new RuntimeException("El correo ya está registrado");
    }
    // ... flujo secuencial ...
    return new AuthResponseDto(token, mapper.toUserDto(saved));
}
```
* **Puntos de Decisión ($D$):** 1 (el `if` del correo registrado).
* **Cálculo:** $M = 1 + 1 = 2$.
* **Mapeo de Pruebas Unitarias Mínimas (2):**
  1. *Camino A (Éxito):* Correo no existe, registra con éxito.
  2. *Camino B (Fallo):* Correo existe, arroja excepción `RuntimeException`.

---

### Método 2: `login`
```java
public AuthResponseDto login(LoginRequestDto dto) {
    UserAccount user = repository.findByEmail(dto.email()) // <--- 1 punto de decisión (orElseThrow / if implícito)
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

    if (!passwordEncoder.matches(dto.password(), user.getPassword())) { // <--- 2do punto de decisión (if)
        throw new RuntimeException("Credenciales inválidas");
    }
    // ... flujo secuencial ...
    return new AuthResponseDto(token, mapper.toUserDto(user));
}
```
* **Puntos de Decisión ($D$):** 2 (el `orElseThrow` que actúa como bifurcación por error, y el `if` de verificación de contraseña).
* **Cálculo:** $M = 2 + 1 = 3$.
* **Mapeo de Pruebas Unitarias Mínimas (3):**
  1. *Camino A (Fallo 1):* Correo no registrado (salta en `orElseThrow`).
  2. *Camino B (Fallo 2):* Correo registrado, pero contraseña inválida (salta en el `if`).
  3. *Camino C (Éxito):* Correo registrado y contraseña correcta.

---

### Método 3: `verifyPassword`
```java
public boolean verifyPassword(Long id, String rawPassword) {
    UserAccount user = repository.findById(id) // <--- 1 punto de decisión (orElseThrow)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    return passwordEncoder.matches(rawPassword, user.getPassword());
}
```
* **Puntos de Decisión ($D$):** 1 (el `orElseThrow`).
* **Cálculo:** $M = 1 + 1 = 2$.
* **Mapeo de Pruebas Unitarias Mínimas (2):**
  1. *Camino A (Fallo):* Usuario no encontrado (excepción).
  2. *Camino B (Éxito/Flujo Normal):* Usuario encontrado, retorna validación boolean (se prueban sub-rutas `true` y `false` para robustez).

---

### Método 4: `findById`
```java
public AuthResponseDto.UserDto findById(Long id) {
    UserAccount user = repository.findById(id) // <--- 1 punto de decisión (orElseThrow)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    return mapper.toUserDto(user);
}
```
* **Puntos de Decisión ($D$):** 1 (el `orElseThrow`).
* **Cálculo:** $M = 1 + 1 = 2$.
* **Mapeo de Pruebas Unitarias Mínimas (2):**
  1. *Camino A (Fallo):* Usuario no encontrado (excepción).
  2. *Camino B (Éxito):* Usuario encontrado y devuelto correctamente.

---

### Método 5: `deleteById`
```java
public void deleteById(Long id) {
    repository.deleteById(id);
}
```
* **Puntos de Decisión ($D$):** 0 (flujo puramente lineal sin bifurcaciones).
* **Cálculo:** $M = 0 + 1 = 1$.
* **Mapeo de Pruebas Unitarias Mínimas (1):**
  1. *Camino Único:* Se invoca el borrado correctamente.

---

## 3. Resumen Final de Cobertura de Caminos
Sumando la complejidad ciclomática de todos los métodos de la clase, obtenemos el número ideal de pruebas que se deben realizar:

| Método | Puntos de Decisión ($D$) | Complejidad Ciclomática ($M$) | Pruebas Requeridas |
| :--- | :---: | :---: | :---: |
| `register` | 1 | 2 | 2 |
| `login` | 2 | 3 | 3 |
| `verifyPassword` | 1 | 2 | 2 |
| `findById` | 1 | 2 | 2 |
| `deleteById` | 0 | 1 | 1 |
| **Total Clase** | **5** | **10** | **10 pruebas** |

### Coincidencia con la Suite de Pruebas Real:
Si revisamos la clase de pruebas unitarias real de nuestro repositorio [UserAccountServiceTest.java](file:///c:/Users/xkait/Desktop/Taller/Catch-Go/Producto/backend/services/auth-service/src/test/java/com/catchandgo/auth/service/UserAccountServiceTest.java), cuenta con **11 pruebas unitarias** configuradas. 

Esto significa que no solo cubre el 100% de la complejidad ciclomática requerida (10 casos de prueba), sino que añade 1 caso extra para evaluar exhaustivamente los comportamientos booleanos de `verifyPassword` (éxito y fallo), asegurando una **cobertura de caminos (Path Coverage) del 100%**.
