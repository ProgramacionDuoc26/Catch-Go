package cl.catchgo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.core.error.ErrorMapper
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobFilter
import cl.catchgo.app.domain.repository.JobsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val jobsRepository: JobsRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<JobCategory?>(null)
    private val refreshing = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val offersFlow = selectedCategory.flatMapLatest { category ->
        jobsRepository.observeOffers(JobFilter(category = category))
    }

    val state: StateFlow<FeedUiState> = combine(
        offersFlow,
        selectedCategory,
        refreshing
    ) { offers, category, isRefreshing ->
        FeedUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            offers = offers,
            selectedCategory = category
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FeedUiState(isLoading = true)
    )

    init {
        refresh()
    }

    fun onCategorySelect(category: JobCategory?) {
        selectedCategory.value = category
    }

    fun onRefresh() {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            jobsRepository.refresh()
                .onFailure { /* fallback silencioso: cache local sigue mostrando lo anterior */ }
            refreshing.value = false
        }
    }
}
