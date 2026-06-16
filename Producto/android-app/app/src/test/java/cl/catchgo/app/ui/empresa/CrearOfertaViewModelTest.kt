package cl.catchgo.app.ui.empresa

import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.data.remote.dto.CreateJobOfferRequest
import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class CrearOfertaViewModelTest {

    // Mock de JobsApi (Retrofit interface)
    private val jobsApi: JobsApi = mockk()

    // Mock de SessionStore (DataStore wrapper)
    private val sessionStore: SessionStore = mockk()

    // Instancia del ViewModel bajo prueba
    private lateinit var viewModel: CrearOfertaViewModel

    // Dispatcher para control de corutinas en pruebas locales
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CrearOfertaViewModel(jobsApi, sessionStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * CP-12 / CP-13: Validar que el formulario de creación de ofertas inicie vacío y limpio.
     */
    @Test
    fun initialState_isCorrectAndEmpty() {
        val state = viewModel.state.value
        assertEquals("", state.titulo)
        assertEquals("", state.descripcion)
        assertEquals("Santiago, RM", state.ubicacion)
        assertEquals("Guardia", state.categoria)
        assertEquals("", state.remuneracion)
        assertEquals("", state.fechaInicio)
        assertEquals("", state.fechaFin)
        assertFalse(state.isLoading)
        assertFalse(state.success)
        assertNull(state.error)
        assertNull(state.latitude)
        assertNull(state.longitude)
    }

    /**
     * CP-12: Validar el correcto guardado en el estado interno al interactuar con el formulario.
     */
    @Test
    fun whenFieldsChange_updatesStateCorrectly() {
        viewModel.onTituloChange("Guardia Nocturno")
        viewModel.onDescripcionChange("Se busca conserje para turnos de noche.")
        viewModel.onUbicacionChange("Providencia, Santiago")
        viewModel.onCategoriaChange("Conserje")
        viewModel.onRemuneracionChange("4500")
        viewModel.onFechaInicioChange("2026-06-20")
        viewModel.onFechaFinChange("2026-06-25")
        viewModel.onLocationChange(-33.42, -70.61)

        val state = viewModel.state.value
        assertEquals("Guardia Nocturno", state.titulo)
        assertEquals("Se busca conserje para turnos de noche.", state.descripcion)
        assertEquals("Providencia, Santiago", state.ubicacion)
        assertEquals("Conserje", state.categoria)
        assertEquals("4500", state.remuneracion)
        assertEquals("2026-06-20", state.fechaInicio)
        assertEquals("2026-06-25", state.fechaFin)
        assertEquals(-33.42, state.latitude!!, 0.0)
        assertEquals(-70.61, state.longitude!!, 0.0)
    }

    /**
     * CP-13: Validar que al intentar publicar faltando campos obligatorios se rechace y muestre error local.
     */
    @Test
    fun submit_withMissingFields_updatesStateWithError() {
        // Cargar campos incompletos (falta fechaInicio)
        viewModel.onTituloChange("Conserje")
        viewModel.onDescripcionChange("Turno temporal")
        viewModel.onRemuneracionChange("4000")
        
        viewModel.submit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.success)
        assertEquals("Completa todos los campos obligatorios", state.error)
        coVerify(exactly = 0) { jobsApi.create(any()) }
    }

    /**
     * CP-12: Validar la publicación exitosa de una oferta laboral invocando la API de Jobs.
     */
    @Test
    fun submit_onSuccess_callsApiAndUpdatesStateToSuccess() {
        // Cargar campos válidos
        viewModel.onTituloChange("Conserje")
        viewModel.onDescripcionChange("Apoyo nocturno")
        viewModel.onRemuneracionChange("4000")
        viewModel.onFechaInicioChange("2026-06-20")
        viewModel.onLocationChange(-33.42, -70.61)

        // Simular sesión de empresa activa
        val userSession = UserSession(
            token = "token-empresa",
            user = User(id = "123", email = "empresa@email.com", role = UserRole.EMPRESA, fullName = "Empresa Test")
        )
        coEvery { sessionStore.session } returns flowOf(userSession)

        val request = CreateJobOfferRequest(
            titulo = "Conserje",
            descripcion = "Apoyo nocturno",
            ubicacion = "Santiago, RM",
            categoria = "Guardia",
            remuneracion = 4000,
            fechaInicio = "2026-06-20",
            fechaFin = null,
            empresaId = "123",
            latitude = -33.42,
            longitude = -70.61
        )

        // Mockear respuesta exitosa de la API
        coEvery { jobsApi.create(request) } returns mockk() // create devuelve Void o un DTO, mockk() simula retorno correcto

        viewModel.submit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.success)
        assertNull(state.error)
        coVerify(exactly = 1) { jobsApi.create(request) }
    }

    /**
     * CP-13: Validar el flujo de fallo en la API y el despliegue del mensaje de error del servidor.
     */
    @Test
    fun submit_onFailure_updatesStateWithErrorMsg() {
        viewModel.onTituloChange("Conserje")
        viewModel.onDescripcionChange("Apoyo nocturno")
        viewModel.onRemuneracionChange("4000")
        viewModel.onFechaInicioChange("2026-06-20")

        val userSession = UserSession(
            token = "token-empresa",
            user = User(id = "123", email = "empresa@email.com", role = UserRole.EMPRESA, fullName = "Empresa Test")
        )
        coEvery { sessionStore.session } returns flowOf(userSession)

        val request = CreateJobOfferRequest(
            titulo = "Conserje",
            descripcion = "Apoyo nocturno",
            ubicacion = "Santiago, RM",
            categoria = "Guardia",
            remuneracion = 4000,
            fechaInicio = "2026-06-20",
            fechaFin = null,
            empresaId = "123",
            latitude = null,
            longitude = null
        )

        // Mockear excepción HTTP o de red en la API
        val exceptionMsg = "Límite de publicaciones alcanzado para el plan actual"
        coEvery { jobsApi.create(request) } throws IOException(exceptionMsg)

        viewModel.submit()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.success)
        assertEquals(exceptionMsg, state.error)
        coVerify(exactly = 1) { jobsApi.create(request) }
    }
}
