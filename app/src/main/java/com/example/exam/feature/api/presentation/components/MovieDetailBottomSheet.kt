
package com.example.exam.feature.api.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.exam.data.remote.model.CastMemberDto
import com.example.exam.data.remote.model.MovieDetailsDto
import com.example.exam.data.remote.model.MovieDto
import com.example.exam.ui.theme.AppSurface
import com.example.exam.ui.theme.AppSurfaceVariant
import com.example.exam.ui.theme.MovieRating
import com.example.exam.ui.theme.PrimaryBlue
import com.example.exam.ui.theme.PrimaryContainer
import com.example.exam.ui.theme.SecondaryRose
import com.example.exam.ui.theme.SignalAmber
import com.example.exam.ui.theme.SignalAmberBg
import com.example.exam.ui.theme.SignalRed
import com.example.exam.ui.theme.SignalRedBg
import com.example.exam.ui.theme.TextMuted
import com.example.exam.ui.theme.TextPrimary
import com.example.exam.ui.theme.TextSecondary
import com.example.exam.ui.theme.Wine

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailScreen(
    movieDetail: MovieDetailsDto?,
    isLoading: Boolean,
    errorMessage: String?,
    isFavourite: Boolean,
    onFavouriteToggle: (MovieDto) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSurface)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SecondaryRose)
                }
            }

            errorMessage != null || movieDetail == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "Failed to load movie details",
                        color = SignalRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // 1. CINEMATIC BACKDROP
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Wine)
                    ) {
                        if (!movieDetail.fullBackdropUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(movieDetail.fullBackdropUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = movieDetail.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Bottom gradient for smooth transition
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Transparent,
                                            AppSurface.copy(alpha = 0.40f),
                                            AppSurface
                                        )
                                    )
                                )
                        )

                        // Back button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(44.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.55f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }
                    }

                    // 2. POSTER AND MOVIE INFORMATION
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Card(
                            modifier = Modifier
                                .width(112.dp)
                                .height(165.dp),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(movieDetail.fullPosterUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = movieDetail.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = movieDetail.title ?: "Untitled",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (!movieDetail.tagline.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = "\"${movieDetail.tagline}\"",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Rating
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SignalAmberBg,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 9.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MovieRating,
                                            modifier = Modifier.size(15.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text(
                                            text = "${String.format("%.1f", movieDetail.voteAverage ?: 0.0)} / 10",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SignalAmber
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "(${movieDetail.voteCount ?: 0} votes)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Release date
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = movieDetail.releaseDate ?: "N/A",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Runtime
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = movieDetail.formattedRuntime,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 3. FAVOURITE BUTTON
                    Button(
                        onClick = {
                            val movieDto = MovieDto(
                                id = movieDetail.id,
                                title = movieDetail.title,
                                originalTitle = movieDetail.title,
                                overview = movieDetail.overview,
                                posterPath = movieDetail.posterPath,
                                backdropPath = movieDetail.backdropPath,
                                releaseDate = movieDetail.releaseDate,
                                voteAverage = movieDetail.voteAverage,
                                voteCount = movieDetail.voteCount,
                                popularity = movieDetail.popularity,
                                originalLanguage = movieDetail.originalLanguage
                            )

                            onFavouriteToggle(movieDto)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavourite) SignalRedBg else PrimaryBlue,
                            contentColor = if (isFavourite) SignalRed else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isFavourite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Text(
                            text = if (isFavourite) {
                                "Remove from Favourites"
                            } else {
                                "Add to Favourites"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 4. GENRES
                    val genres = movieDetail.genres

                    if (!genres.isNullOrEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            genres.forEach { genre ->
                                Surface(
                                    color = PrimaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = genre.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Wine,
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))
                    }

                    // 5. OVERVIEW
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Overview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = movieDetail.overview ?: "No overview available.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // 6. PRODUCTION INFORMATION
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppSurfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            InfoRow(
                                label = "Director",
                                value = movieDetail.directorName
                            )

                            InfoRow(
                                label = "Original Language",
                                value = (movieDetail.originalLanguage ?: "en").uppercase()
                            )

                            val companies = movieDetail.productionCompanies
                                ?.map { it.name }
                                ?.take(3)

                            if (!companies.isNullOrEmpty()) {
                                InfoRow(
                                    label = "Production",
                                    value = companies.joinToString(", ")
                                )
                            }
                        }
                    }

                    // 7. CAST AND CREW
                    val cast = movieDetail.credits?.cast?.take(15)

                    if (!cast.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(26.dp))

                        Text(
                            text = "Cast & Characters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            items(cast) { member ->
                                CastItem(member)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CastItem(member: CastMemberDto) {
    Column(
        modifier = Modifier.width(82.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!member.fullProfileUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(member.fullProfileUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = member.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = member.character ?: "",
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
