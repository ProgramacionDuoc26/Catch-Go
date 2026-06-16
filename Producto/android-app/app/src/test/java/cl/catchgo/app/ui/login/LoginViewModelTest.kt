package cl.catchgo.app.ui.login

import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
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
class LoginViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * CP-26 (Login): Verificar que el estado inicial de LoginUiState comience vacío y bloqueado.
     */
    @Test
    fun initialState_isCorrect() {
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.canSubmit)
    }

    /**
     * CP-26 (Login): Verificar la actualización del campo de email en el estado.
     */
    @Test
    fun onEmailChange_updatesState() {
        viewModel.onEmailChange("test@email.com")
        val state = viewModel.state.value
        assertEquals("test@email.com", state.email)
        assertNull(state.errorMessage)
    }

    /**
     * CP-26 (Login): Verificar la actualización del campo de contraseña en el estado.
     */
    @Test
    fun onPasswordChange_updatesState() {
        viewModel.onPasswordChange("password123")
        val state = viewModel.state.value
        assertEquals("password123", state.password)
        assertNull(state.errorMessage)
    }

    /**
     * CP-26 (Login): Verificar que canSubmit sea verdadero solo cuando ambos campos están completos.
     */
    @Test
    fun canSubmit_isTrueWhenEmailAndPasswordAreNotBlank() {
        viewModel.onEmailChange("test@email.com")
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onPasswordChange("password")
        assertTrue(viewModel.state.value.canSubmit)
    }

    /**
     * CP-07: Verificar que si el formulario es inválido no se invoque la llamada al repositorio de autenticación.
     */
    @Test
    fun onSubmit_whenCanSubmitIsFalse_doesNotCallLogin() {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.onSubmit()

        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    /**
     * CP-05: Validar el inicio de sesión exitoso de un trabajador y la correcta propagación de estados.
     */
    @Test
    fun onSubmit_onSuccess_updatesLoadingState() {
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("password123")

        val userSession = UserSession(
            token = "jwt-token",
            user = User(
                id = "1",
                email = "test@email.com",
                role = UserRole.WORKER,
                fullName = "Test User"
            )
        )

        coEvery { authRepository.login("test@email.com", "password123") } returns Result.success(userSession)

        viewModel.onSubmit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        coVerify(exactly = 1) { authRepository.login("test@email.com", "password123") }
    }

    /**
     * CP-07: Validar que el inicio de sesión falle con el mensaje de error correspondiente ante credenciales inválidas.
     */
    @Test
    fun onSubmit_onFailure_updatesStateWithError() {
        viewModel.onEmailChange("test@email.com")
        viewModel.onPasswordChange("password123")

        val exception = IOException("No internet connection")
        coEvery { authRepository.login("test@email.com", "password123") } returns Result.failure(exception)

        viewModel.onSubmit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Sin conexión a internet", state.errorMessage)
        coVerify(exactly = 1) { authRepository.login("test@email.com", "password123") }
    }

    /**
     * CP-05 (Google): Validar la autenticación exitosa mediante proveedor de terceros Google.
     */
    @Test
    fun loginGoogle_onSuccess_updatesState() {
        val userSession = UserSession(
            token = "google-jwt",
            user = User(
                id = "2",
                email = "google@email.com",
                role = UserRole.WORKER,
                fullName = "Google User"
            )
        )

        coEvery { authRepository.loginGoogle("google@email.com", "Google User") } returns Result.success(userSession)

        viewModel.loginGoogle("google@email.com", "Google User")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        coVerify(exactly = 1) { authRepository.loginGoogle("google@email.com", "Google User") }
    }

    /**
     * CP-07 (Google): Validar el manejo de errores de autenticación fallida con Google.
     */
    @Test
    fun loginGoogle_onFailure_updatesStateWithError() {
        val exception = RuntimeException("Google Login Failed")
        coEvery { authRepository.loginGoogle("google@email.com", "Google User") } returns Result.failure(exception)

        viewModel.loginGoogle("google@email.com", "Google User")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Google Login Failed", state.errorMessage)
        coVerify(exactly = 1) { authRepository.loginGoogle("google@email.com", "Google User") }
    }

}
