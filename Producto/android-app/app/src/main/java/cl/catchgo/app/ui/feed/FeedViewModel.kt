package cl.catchgo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val offersFlow = selectedCategory.flatMapLatest { category ->
        jobsRepository.observeOffers(JobFilter(category = category))
    }

    val state: StateFlow<FeedUiState> = combine(
        offersFlow,
        selectedCategory,
        refreshing,
        searchQuery
    ) { offers, category, isRefreshing, query ->
        val filtered = if (query.isBlank()) offers
        else offers.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.company.contains(query, ignoreCase = true) ||
                it.comuna.contains(query, ignoreCase = true)
        }
        FeedUiState(
            isLoading = false,
            isRefreshing = isRefreshing,
            offers = filtered,
            selectedCategory = category,
            searchQuery = query
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

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun onRefresh() {
        refresh()
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
