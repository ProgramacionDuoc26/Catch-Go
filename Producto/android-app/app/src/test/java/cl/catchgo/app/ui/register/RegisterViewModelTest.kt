package cl.catchgo.app.ui.register

import cl.catchgo.app.domain.model.RegisterInput
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    // Mock del repositorio de autenticación
    private val authRepository: AuthRepository = mockk()
    
    // Instancia del ViewModel bajo prueba
    private lateinit var viewModel: RegisterViewModel

    // Dispatcher de pruebas para corutinas de Kotlin
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        // Redirigir el despachador de la corutina Main al despachador de pruebas
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        // Restablecer el despachador Main de corutinas
        Dispatchers.resetMain()
    }

    /**
     * CP-27: Validar que el estado inicial del formulario de registro esté vacío y deshabilitado.
     */
    @Test
    fun initialState_isCorrectAndCannotSubmit() {
        val state = viewModel.state.value
        assertNull(state.role)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.fullName)
        assertEquals("", state.rut)
        assertEquals("", state.phone)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.canSubmit)
    }

    /**
     * CP-28: Validar la correcta habilitación del botón de envío (canSubmit) cuando todos los campos son válidos.
     */
    @Test
    fun whenValidDataInputted_canSubmitIsTrue() {
        // Simular ingreso de datos válidos paso a paso
        viewModel.onFullNameChange("Juan Pérez")
        viewModel.onEmailChange("juan.perez@email.com")
        viewModel.onRutChange("19123456-7")
        viewModel.onPhoneChange("+56912345678")
        viewModel.onPasswordChange("Password123") // 11 caracteres (mínimo 6)
        viewModel.onRoleChange = viewModel.onRoleSelect(UserRole.WORKER)

        // Comprobar que el formulario es válido para envío
        val state = viewModel.state.value
        assertTrue(state.canSubmit)
    }

    /**
     * CP-28 (Alterno): Validar que la contraseña deba tener al menos 6 caracteres para poder enviar el formulario.
     */
    @Test
    fun whenPasswordIsLessThanSixChars_canSubmitIsFalse() {
        viewModel.onFullNameChange("Juan Pérez")
        viewModel.onEmailChange("juan.perez@email.com")
        viewModel.onRutChange("19123456-7")
        viewModel.onPhoneChange("+56912345678")
        viewModel.onPasswordChange("12345") // 5 caracteres (inválido)
        viewModel.onRoleSelect(UserRole.WORKER)

        val state = viewModel.state.value
        assertFalse(state.canSubmit)
    }

    /**
     * CP-29: Validar el flujo de registro exitoso invocando la API de autenticación.
     */
    @Test
    fun onSubmit_onSuccess_updatesStateCorrectly() {
        // Cargar campos válidos
        viewModel.onFullNameChange("Juan Pérez")
        viewModel.onEmailChange("juan.perez@email.com")
        viewModel.onRutChange("19123456-7")
        viewModel.onPhoneChange("+56912345678")
        viewModel.onPasswordChange("Password123")
        viewModel.onRoleSelect(UserRole.WORKER)

        val input = RegisterInput(
            email = "juan.perez@email.com",
            password = "Password123",
            fullName = "Juan Pérez",
            rut = "19123456-7",
            phone = "+56912345678",
            role = UserRole.WORKER
        )

        // Mockear respuesta exitosa del repositorio
        coEvery { authRepository.register(input) } returns Result.success(true)

        // Ejecutar envío
        viewModel.onSubmit()

        // Comprobar estados finales
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        coVerify(exactly = 1) { authRepository.register(input) }
    }

    /**
     * CP-30: Validar la gestión de errores en el registro ante fallas de red del servidor.
     */
    @Test
    fun onSubmit_onFailure_updatesStateWithError() {
        viewModel.onFullNameChange("Juan Pérez")
        viewModel.onEmailChange("juan.perez@email.com")
        viewModel.onRutChange("19123456-7")
        viewModel.onPhoneChange("+56912345678")
        viewModel.onPasswordChange("Password123")
        viewModel.onRoleSelect(UserRole.WORKER)

        val input = RegisterInput(
            email = "juan.perez@email.com",
            password = "Password123",
            fullName = "Juan Pérez",
            rut = "19123456-7",
            phone = "+56912345678",
            role = UserRole.WORKER
        )

        // Mockear excepción de red (IOException)
        val exception = IOException("Fallo en la comunicación con el servidor")
        coEvery { authRepository.register(input) } returns Result.failure(exception)

        viewModel.onSubmit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Error de conexión", state.errorMessage) // ErrorMapper traduce IOException a 'Error de conexión' o similar
        coVerify(exactly = 1) { authRepository.register(input) }
    }
}
