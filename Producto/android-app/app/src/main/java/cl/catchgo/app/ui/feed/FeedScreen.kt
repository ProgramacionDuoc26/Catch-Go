package cl.catchgo.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.catchgo.app.domain.model.JobCategory
import cl.catchgo.app.domain.model.JobOffer
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.ui.components.EmptyState
import cl.catchgo.app.ui.components.OfferCard
import cl.catchgo.app.ui.components.SkeletonBox
import cl.catchgo.app.ui.feed.FeedPlaceholderScreen
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

@Composable
fun FeedScreen(
    role: UserRole,
    onOfferClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (role) {
        UserRole.WORKER -> WorkerFeedRoute(onOfferClick = onOfferClick, modifier = modifier)
        else -> FeedPlaceholderScreen(role = role, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerFeedRoute(
    onOfferClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        FeedContent(
            state = state,
            onCategorySelect = viewModel::onCategorySelect,
            onOfferClick = onOfferClick
        )
    }
}

@Composable
private fun FeedContent(
    state: FeedUiState,
    onCategorySelect: (JobCategory?) -> Unit,
    onOfferClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FeedHeader(count = if (state.isLoading) null else state.offers.size)
        CategoryChipsRow(
            selected = state.selectedCategory,
            onSelect = onCategorySelect
        )
        when {
            state.isLoading -> SkeletonList()
            state.offers.isEmpty() -> EmptyFeed(filteredBy = state.selectedCategory)
            else -> OffersList(offers = state.offers, onOfferClick = onOfferClick)
        }
    }
}

@Composable
private fun FeedHeader(count: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
    ) {
        Text(
            text = "Ofertas cerca tuyo",
            style = MaterialTheme.typography.headlineSmall
        )
        if (count != null) {
            Text(
                text = if (count == 1) "1 oferta compatible" else "$count ofertas compatibles",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChipsRow(
    selected: JobCategory?,
    onSelect: (JobCategory?) -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = BrandBlue50,
        selectedLabelColor = BrandBlue700
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.sm)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Todas") },
                colors = chipColors
            )
        }
        items(JobCategory.entries) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(if (selected == category) null else category) },
                label = { Text(category.display) },
                colors = chipColors
            )
        }
    }
}

@Composable
private fun OffersList(
    offers: List<JobOffer>,
    onOfferClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Spacing.lg,
            end = Spacing.lg,
            top = Spacing.xs,
            bottom = Spacing.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(items = offers, key = { it.id }) { offer ->
            OfferCard(
                titulo = offer.title,
                empresa = offer.company,
                comuna = offer.comuna,
                jornada = offer.jornada,
                score = offer.score,
                onClick = { onOfferClick(offer.id) }
            )
        }
    }
}

@Composable
private fun SkeletonList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        repeat(4) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
private fun EmptyFeed(filteredBy: JobCategory?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = if (filteredBy == null) "No hay ofertas disponibles" else "Sin ofertas en ${filteredBy.display}",
            description = if (filteredBy == null)
                "Pronto vamos a tener nuevas oportunidades. Tirá hacia abajo para refrescar."
            else
                "Probá con otra categoría o quitá el filtro.",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(40.dp)
                )
            }
        )
    }
}

@Suppress("unused")
@Composable
private fun FeedSpacer() {
    Spacer(Modifier.height(Spacing.md))
}
