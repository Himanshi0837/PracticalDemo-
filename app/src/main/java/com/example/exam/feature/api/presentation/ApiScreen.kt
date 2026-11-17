
package com.example.exam.feature.api.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.exam.feature.api.presentation.components.MovieCard
import com.example.exam.feature.api.presentation.components.MovieDetailScreen
import com.example.exam.ui.components.BubbleBackground
import com.example.exam.ui.theme.AppBackground
import com.example.exam.ui.theme.AppBorder
import com.example.exam.ui.theme.AppSurface
import com.example.exam.ui.theme.AppSurfaceVariant
import com.example.exam.ui.theme.PrimaryBlue
import com.example.exam.ui.theme.PrimaryContainer
import com.example.exam.ui.theme.SecondaryRose
import com.example.exam.ui.theme.SignalRed
import com.example.exam.ui.theme.TextMuted
import com.example.exam.ui.theme.TextPrimary
import com.example.exam.ui.theme.TextSecondary
import com.example.exam.ui.theme.Wine

@Composable
fun ApiScreen(
    modifier: Modifier = Modifier,
    viewModel: ApiViewModel = hiltViewModel(),
    onMovieDetailVisibilityChanged: (Boolean) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    // Detect when user reaches the end of the movie grid.
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleIndex =
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(
        state.selectedMovieDetail,
        state.isLoadingDetail
    ) {
        onMovieDetailVisibilityChanged(
            state.selectedMovieDetail != null ||
                    state.isLoadingDetail
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
//            .background(AppBackground)
    ) {
        BubbleBackground(
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // ==========================================
            // 1. NEW MOVIE EXPLORER HEADER
            // ==========================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(AppBackground)
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    )
            ) {

                // Title section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Discover Movies",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        Text(
                            text = "Find your next favourite movie",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }

                    // Movie icon
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Search field
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = {
                        viewModel.onSearchQueryChange(it)
                    },
                    placeholder = {
                        Text(
                            text = if (state.searchMode == SearchMode.MOVIES) {
                                "Search movies by title..."
                            } else {
                                "Search movies by actor..."
                            },
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchQueryChange("")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppSurface,
                        unfocusedContainerColor = AppSurface,
                        focusedBorderColor = SecondaryRose,
                        unfocusedBorderColor = AppBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Category heading
                Text(
                    text = "Browse Categories",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category filters
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(MovieCategory.values()) { category ->

                        val isSelected =
                            state.selectedCategory == category &&
                                    state.searchQuery.isEmpty()

                        val countBadge =
                            if (category == MovieCategory.FAVOURITES) {
                                " (${state.favouriteIds.size})"
                            } else {
                                ""
                            }

                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isSelected) {
                                PrimaryBlue
                            } else {
                                AppSurfaceVariant
                            },
                            border = if (isSelected) {
                                null
                            } else {
                                BorderStroke(1.dp, AppBorder)
                            },
                            modifier = Modifier.clickable {
                                viewModel.selectCategory(category)
                            }
                        ) {
                            Text(
                                text = "${category.displayName}$countBadge",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                },
                                color = if (isSelected) {
                                    Color.White
                                } else {
                                    TextSecondary
                                },
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 9.dp
                                )
                            )
                        }
                    }
                }
            }

            // Subtle divider below header
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppBorder.copy(alpha = 0.7f))
            )

            // ==========================================
            // 2. MAIN CONTENT
            // ==========================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {

                    // Loading
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = SecondaryRose
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = "Loading movies...",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Error
                    state.errorMessage != null &&
                            state.movies.isEmpty() -> {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = SignalRed,
                                    modifier = Modifier.size(54.dp)
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = state.errorMessage
                                        ?: "Something went wrong",
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.retry()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    // Empty state
                    state.movies.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(56.dp)
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = if (
                                        state.selectedCategory ==
                                        MovieCategory.FAVOURITES
                                    ) {
                                        "No favourite movies saved yet.\nTap the heart icon on any movie to add it here!"
                                    } else {
                                        "No movies found matching your search."
                                    },
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }

                    // Movie grid
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 20.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.movies,
                                key = { it.id }
                            ) { movie ->

                                val isFav =
                                    state.favouriteIds.contains(movie.id)

                                MovieCard(
                                    movie = movie,
                                    isFavourite = isFav,
                                    onMovieClick = { movieId ->
                                        viewModel.openMovieDetail(movieId)
                                    },
                                    onFavouriteToggle = { selectedMovie ->
                                        viewModel.toggleFavourite(selectedMovie)
                                    }
                                )
                            }

                            // Pagination loading indicator
                            if (state.isPaginationLoading) {
                                item(
                                    span = {
                                        GridItemSpan(2)
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = SecondaryRose,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. MOVIE DETAIL OVERLAY
        // ==========================================

        if (
            state.selectedMovieDetail != null ||
            state.isLoadingDetail
        ) {
            MovieDetailScreen(
                movieDetail = state.selectedMovieDetail,
                isLoading = state.isLoadingDetail,
                errorMessage = state.detailErrorMessage,
                isFavourite = state.selectedMovieDetail?.id
                    ?.let { state.favouriteIds.contains(it) }
                    ?: false,
                onFavouriteToggle = { movie ->
                    viewModel.toggleFavourite(movie)
                },
                onDismiss = {
                    viewModel.closeMovieDetail()
                }
            )
        }
    }
}

// ==========================================
// SEARCH MODE PILL
// ==========================================

@Composable
fun SearchModePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryContainer else AppSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Wine else AppBorder
        ),
        modifier = modifier
            .height(36.dp)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Wine else TextSecondary,
                modifier = Modifier.size(15.dp)
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = if (isSelected) Wine else TextSecondary
            )
        }
    }
}
