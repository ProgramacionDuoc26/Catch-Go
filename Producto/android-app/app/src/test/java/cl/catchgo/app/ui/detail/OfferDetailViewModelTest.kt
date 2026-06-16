package cl.catchgo.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.JobsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class OfferDetailViewModelTest {

    // Mock de JobsRepository
    private val jobsRepository: JobsRepository = mockk()

    // Mock de ApplicationsRepository
    private val applicationsRepository: ApplicationsRepository = mockk()

    // Instancia del ViewModel bajo prueba
    private lateinit var viewModel: OfferDetailViewModel

    // Dispatcher para corutinas en pruebas locales
    private val testDispatcher = UnconfinedTestDispatcher()

    // Datos de oferta simulados
    private val mockOffer = JobOffer(
        id = "101",
        title = "Oferta Guardia",
        company = "Seguridad total",
        category = JobCategory.GUARDIA,
        region = "Metropolitana",
        comuna = "Santiago",
        jornada = "Part-time",
        scheduleText = "Sábado y Domingo",
        salaryClp = 5000,
        salaryUnit = "hr",
        description = "Turno en mall.",
        requirements = listOf("OS-10 al día"),
        score = 80,
        latitude = -33.45,
        longitude = -70.65
    )

    // Job para mantener activa la recolección del flow state
    private lateinit var collectJob: Job

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Configurar respuesta inicial por defecto del repositorio de postulaciones (observador de mis postulaciones)
        every { applicationsRepository.observeMyApplications() } returns flowOf(emptyList())

        // Configurar respuesta inicial de éxito de la oferta en el init
        coEvery { jobsRepository.getOffer("101") } returns Result.success(mockOffer)

        // Instanciar ViewModel con el id de oferta 101 en SavedStateHandle
        val savedStateHandle = SavedStateHandle(mapOf("id" to "101"))
        viewModel = OfferDetailViewModel(jobsRepository, applicationsRepository, savedStateHandle)

        // Activar WhileSubscribed recolectando el flow
        collectJob = viewModel.viewModelScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
    }

    @After
    fun tearDown() {
        if (::collectJob.isInitialized) {
            collectJob.cancel()
        }
        Dispatchers.resetMain()
    }

    /**
     * CP-47: Verificar la carga exitosa del detalle de la oferta de trabajo en el inicio.
     */
    @Test
    fun init_onSuccess_loadsOfferDetailsAndSetsState() {
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(mockOffer, state.offer)
        assertFalse(state.isApplied)
        assertNull(state.activeApplication)
        coVerify(exactly = 1) { jobsRepository.getOffer("101") }
    }

    /**
     * CP-48: Verificar la visualización correcta del mensaje de error cuando falla la carga del detalle.
     */
    @Test
    fun init_onFailure_updatesStateWithErrorMessage() {
        // Mockear fallo en la obtención de la oferta
        val exception = IOException("Fallo al conectar con microservicio Jobs")
        coEvery { jobsRepository.getOffer("102") } returns Result.failure(exception)

        every { applicationsRepository.observeMyApplications() } returns flowOf(emptyList())

        val savedStateHandle = SavedStateHandle(mapOf("id" to "102"))
        val vmError = OfferDetailViewModel(jobsRepository, applicationsRepository, savedStateHandle)

        // Activar WhileSubscribed recolectando el flow
        val job = vmError.viewModelScope.launch(testDispatcher) {
            vmError.state.collect {}
        }

        val state = vmError.state.value
        assertFalse(state.isLoading)
        assertEquals("Sin conexión a internet", state.errorMessage) // ErrorMapper mapea IOException a 'Sin conexión a internet'
        job.cancel()
    }

    /**
     * CP-49: Validar la postulación exitosa del usuario a una oferta de empleo.
     */
    @Test
    fun submitApplication_onSuccess_sendsEventAndSetsLoadingFalse() {
        // Mockear respuesta exitosa de postulación
        coEvery { applicationsRepository.apply("101", "Me interesa el turno") } returns Result.success(mockk())

        viewModel.submitApplication("Me interesa el turno")

        val state = viewModel.state.value
        assertFalse(state.isApplying)
        assertNull(state.applyError)
        coVerify(exactly = 1) { applicationsRepository.apply("101", "Me interesa el turno") }
    }

    /**
     * CP-50: Validar la gestión de errores locales ante fallas al intentar postular.
     */
    @Test
    fun submitApplication_onFailure_updatesStateWithApplyError() {
        // Mockear excepción en la postulación
        val exception = IOException("Error al postular")
        coEvery { applicationsRepository.apply("101", "Mensaje") } returns Result.failure(exception)

        viewModel.submitApplication("Mensaje")

        val state = viewModel.state.value
        assertFalse(state.isApplying)
        assertEquals("Sin conexión a internet", state.applyError)
        coVerify(exactly = 1) { applicationsRepository.apply("101", "Mensaje") }
    }

    /**
     * CP-51: Validar que se pueda cancelar una postulación de forma exitosa.
     */
    @Test
    fun cancelPostulation_onSuccess_sendsEventAndSetsActionLoadingFalse() {
        val applicationId = 999L
        // Mockear cancelación exitosa
        coEvery { applicationsRepository.cancelApplication(applicationId) } returns Result.success(mockk())

        viewModel.cancelPostulation(applicationId)

        val state = viewModel.state.value
        assertFalse(state.isActionLoading)
        assertNull(state.actionError)
        coVerify(exactly = 1) { applicationsRepository.cancelApplication(applicationId) }
    }
}
