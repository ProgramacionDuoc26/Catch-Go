package cl.catchgo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.domain.repository.JobsRepository
import cl.catchgo.app.domain.repository.ProfileRepository
import cl.catchgo.app.domain.repository.ApplicationsRepository
import cl.catchgo.app.util.MatchEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val jobsRepository: JobsRepository,
    private val sessionStore: SessionStore,
    private val profileRepository: ProfileRepository,
    private val applicationsRepository: ApplicationsRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<JobCategory?>(null)
    private val refreshing = MutableStateFlow(false)
    private val searchQuery = MutableStateFlow("")
    private val profileCompletion = MutableStateFlow(0)
    private val activeTab = MutableStateFlow(FeedTab.EXPLORAR)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val offersFlow = selectedCategory.flatMapLatest { category ->
        jobsRepository.observeOffers(JobFilter(category = category))
    }

    val state: StateFlow<FeedUiState> = combine(
        offersFlow,
        selectedCategory,
        refreshing,
        searchQuery,
        profileCompletion,
        applicationsRepository.observeMyApplications(),
        activeTab
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val offers = array[0] as List<cl.catchgo.app.domain.model.JobOffer>
        val category = array[1] as cl.catchgo.app.domain.model.JobCategory?
        val isRefreshing = array[2] as Boolean
        val query = array[3] as String
        val completion = array[4] as Int
        @Suppress("UNCHECKED_CAST")
        val apps = array[5] as List<cl.catchgo.app.domain.model.JobApplication>
        val currentTab = array[6] as FeedTab

        // Calculate counts based on backend rawStatus string
        val postCount = apps.count { it.rawStatus in listOf("PENDIENTE", "ACEPTADO", "TRABAJO_FINALIZADO") }
        val pagCount = apps.count { it.rawStatus in listOf("PAGO_ENVIADO", "PAGO_DISPUTADO") }
        val califCount = apps.count { it.rawStatus in listOf("PAGO_CONFIRMADO", "CALIFICADO_EMPRESA") }
        val compCount = apps.count { it.rawStatus in listOf("CALIFICADO_TRABAJADOR", "FINALIZADA", "RECHAZADO", "ARCHIVADA") }

        // Filter offers matching category and search query
        val filteredOffers = if (query.isBlank()) offers
        else offers.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.company.contains(query, ignoreCase = true) ||
                it.comuna.contains(query, ignoreCase = true)
        }
        val sortedOffers = filteredOffers.sortedByDescending { it.id.toLongOrNull() ?: 0L }

        // Filter applications based on selected tab and search query
        val filteredApps = when (currentTab) {
            FeedTab.POSTULACIONES -> apps.filter { it.rawStatus in listOf("PENDIENTE", "ACEPTADO", "TRABAJO_FINALIZADO") }
            FeedTab.PAGOS -> apps.filter { it.rawStatus in listOf("PAGO_ENVIADO", "PAGO_DISPUTADO") }
            FeedTab.POR_CALIFICAR -> apps.filter { it.rawStatus in listOf("PAGO_CONFIRMADO", "CALIFICADO_EMPRESA") }
            FeedTab.COMPLETADAS -> apps.filter { it.rawStatus in listOf("CALIFICADO_TRABAJADOR", "FINALIZADA", "RECHAZADO", "ARCHIVADA") }
            else -> emptyList()
        }.filter {
            query.isBlank() ||
                it.offerTitle.contains(query, ignoreCase = true) ||
                it.company.contains(query, ignoreCase = true) ||
                it.comuna.contains(query, ignoreCase = true)
        }

        FeedUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            offers = sortedOffers,
            selectedCategory = category,
            searchQuery = query,
            profileCompletion = completion,
            activeTab = currentTab,
            applications = apps,
            filteredApplications = filteredApps,
            postulacionesCount = postCount,
            pagosCount = pagCount,
            porCalificarCount = califCount,
            completadasCount = compCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FeedUiState(isLoading = true)
    )

    init {
        refresh()
        loadProfileCompletion()
        viewModelScope.launch {
            applicationsRepository.refreshFromBackend()
        }
    }

    private fun loadProfileCompletion() {
        viewModelScope.launch {
            val userId = sessionStore.session.first()?.user?.id ?: return@launch
            profileRepository.getProfile(userId).onSuccess { profile ->
                if (profile != null) {
                    profileCompletion.value = MatchEngine.calculateProfileCompletion(profile)
                }
            }
        }
    }

    fun onCategorySelect(category: JobCategory?) {
        selectedCategory.value = category
    }

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun onRefresh() {
        refresh()
        viewModelScope.launch {
            applicationsRepository.refreshFromBackend()
        }
    }

    fun onTabSelect(tab: FeedTab) {
        activeTab.value = tab
    }

    private fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            jobsRepository.refresh()
                .onFailure { }
            refreshing.value = false
        }
    }
}
