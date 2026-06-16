package cl.catchgo.app.ui.feed

import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.model.User
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.domain.repository.JobsRepository
import cl.catchgo.app.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val jobsRepository: JobsRepository = mockk(relaxed = true)
    private val sessionStore: SessionStore = mockk(relaxed = true)
    private val profileRepository: ProfileRepository = mockk(relaxed = true)
    private val applicationsRepository: ApplicationsRepository = mockk(relaxed = true)

    private val offersFlow = MutableStateFlow<List<JobOffer>>(emptyList())
    private val myAppsFlow = MutableStateFlow<List<JobApplication>>(emptyList())
    private val sessionFlow = MutableStateFlow<UserSession?>(null)

    private lateinit var viewModel: FeedViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val collectJobs = mutableListOf<Job>()

    private val mockProfile = ProfileRemoteDto(
        id = 1L,
        userId = "usr-123",
        name = "Juan Perez",
        email = "trabajador@test.com",
        phone = "+56912345678",
        birthDate = "1990-01-01",
        bankName = "Banco Estado",
        accountType = "Rut",
        accountNumber = "12345678",
        photoUrl = "http://photo",
        cvUrl = "http://cv",
        skills = """{"habilidades":["electricidad"],"ambiente":"interior","caracteristica":"responsable","preferencia":"dia"}""",
        plan = "PREMIUM"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mockear las llamadas de flujos del repositorio y almacén de sesión
        every { jobsRepository.observeOffers(any()) } returns offersFlow
        every { applicationsRepository.observeMyApplications() } returns myAppsFlow
        every { sessionStore.session } returns sessionFlow

        // Simular que el usuario tiene una sesión activa
        sessionFlow.value = UserSession(
            token = "dummy-token",
            user = User(
                id = "usr-123",
                email = "trabajador@test.com",
                role = UserRole.WORKER,
                fullName = "Juan Perez",
                nivel = 1
            )
        )

        // Configurar retornos de perfiles y refrescos
        coEvery { profileRepository.getProfile("usr-123") } returns Result.success(mockProfile)
        coEvery { jobsRepository.refresh() } returns Result.success(Unit)
        coEvery { applicationsRepository.refreshFromBackend() } returns Unit

        // Instanciar el ViewModel
        viewModel = FeedViewModel(jobsRepository, sessionStore, profileRepository, applicationsRepository)

        // Recolectar el StateFlow para activar la recolección WhileSubscribed(5_000)
        val job = viewModel.viewModelScope.launch(testDispatcher) {
            viewModel.state.collect {}
        }
        collectJobs.add(job)
    }

    @After
    fun tearDown() {
        collectJobs.forEach { it.cancel() }
        collectJobs.clear()
        Dispatchers.resetMain()
    }

    /**
     * CP-57: Verificar la carga exitosa del estado inicial del feed (listado de ofertas y completitud del perfil).
     */
    @Test
    fun loadInitialState_success() {
        val offer = JobOffer(
            id = "1", title = "Guardia de seguridad", company = "Securitas", category = JobCategory.GUARDIA,
            region = "Metropolitana", comuna = "Providencia", jornada = "Part-time", scheduleText = "Sábado y Domingo",
            salaryClp = 4000, salaryUnit = "hr", description = "Vigilancia", requirements = emptyList(), score = 50,
            latitude = -33.42, longitude = -70.61
        )
        offersFlow.value = listOf(offer)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.offers.size)
        assertEquals("Guardia de seguridad", state.offers[0].title)
        assertTrue(state.profileCompletion > 0)
        coVerify(exactly = 1) { profileRepository.getProfile("usr-123") }
    }

    /**
     * CP-58: Verificar que la selección de una categoría de trabajo actualice el filtro correspondiente en el estado.
     */
    @Test
    fun onCategorySelect_updatesCategoryFilter() {
        viewModel.onCategorySelect(JobCategory.CONSERJE)

        verify { jobsRepository.observeOffers(JobFilter(category = JobCategory.CONSERJE)) }
        assertEquals(JobCategory.CONSERJE, viewModel.state.value.selectedCategory)
    }

    /**
     * CP-59: Verificar que la búsqueda por texto filtre las ofertas de trabajo visibles en el feed.
     */
    @Test
    fun onSearchChange_filtersOffersByQuery() {
        val offer1 = JobOffer(
            id = "1", title = "Guardia nocturno", company = "Empresa A", category = JobCategory.GUARDIA,
            region = "M", comuna = "Santiago", jornada = "F", scheduleText = "", salaryClp = 100, salaryUnit = "",
            description = "", requirements = emptyList(), score = 10, latitude = 0.0, longitude = 0.0
        )
        val offer2 = JobOffer(
            id = "2", title = "Conserje diurno", company = "Empresa B", category = JobCategory.CONSERJE,
            region = "M", comuna = "Las Condes", jornada = "F", scheduleText = "", salaryClp = 100, salaryUnit = "",
            description = "", requirements = emptyList(), score = 10, latitude = 0.0, longitude = 0.0
        )
        offersFlow.value = listOf(offer1, offer2)

        // Buscar "Conserje"
        viewModel.onSearchChange("Conserje")

        val state = viewModel.state.value
        assertEquals("Conserje", state.searchQuery)
        assertEquals(1, state.offers.size)
        assertEquals("Conserje diurno", state.offers[0].title)
    }

    /**
     * CP-60: Verificar que la selección de pestañas filtre las postulaciones y actualice los contadores correspondientes.
     */
    @Test
    fun onTabSelect_filtersApplicationsCorrectly() {
        val app1 = JobApplication(
            id = "a1", offerId = "1", offerTitle = "Guardia", company = "Empresa A", comuna = "Santiago",
            message = "Hola", status = ApplicationStatus.PENDING, createdAtIso = "2026-06-15", rawStatus = "PENDIENTE"
        )
        val app2 = JobApplication(
            id = "a2", offerId = "2", offerTitle = "Conserje", company = "Empresa B", comuna = "Las Condes",
            message = "Hola", status = ApplicationStatus.ACCEPTED, createdAtIso = "2026-06-15", rawStatus = "PAGO_ENVIADO"
        )
        myAppsFlow.value = listOf(app1, app2)

        // Validar conteos globales de tabs
        val stateInitial = viewModel.state.value
        assertEquals(1, stateInitial.postulacionesCount) // PENDIENTE
        assertEquals(1, stateInitial.pagosCount) // PAGO_ENVIADO

        // Seleccionar pestaña de POSTULACIONES
        viewModel.onTabSelect(FeedTab.POSTULACIONES)
        val statePost = viewModel.state.value
        assertEquals(FeedTab.POSTULACIONES, statePost.activeTab)
        assertEquals(1, statePost.filteredApplications.size)
        assertEquals("a1", statePost.filteredApplications[0].id)

        // Seleccionar pestaña de PAGOS
        viewModel.onTabSelect(FeedTab.PAGOS)
        val statePagos = viewModel.state.value
        assertEquals(FeedTab.PAGOS, statePagos.activeTab)
        assertEquals(1, statePagos.filteredApplications.size)
        assertEquals("a2", statePagos.filteredApplications[0].id)
    }

    /**
     * CP-61: Verificar que la acción de refrescar el feed gatille llamadas de actualización en los repositorios.
     */
    @Test
    fun onRefresh_triggersRepositoriesRefresh() {
        viewModel.onRefresh()

        coVerify(exactly = 2) { applicationsRepository.refreshFromBackend() } // Una en init, otra en onRefresh
        coVerify(exactly = 2) { jobsRepository.refresh() } // Una en init (refrescando por defecto), otra en onRefresh
    }
}
