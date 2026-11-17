
package com.example.exam.feature.api.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.exam.data.remote.model.MovieDto
import com.example.exam.ui.theme.*

@Composable
fun MovieCard(
    movie: MovieDto,
    isFavourite: Boolean,
    onMovieClick: (Int) -> Unit,
    onFavouriteToggle: (MovieDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (isFavourite) MovieFavourite else Color.White,
        label = "heartColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clickable {
                onMovieClick(movie.id)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Wine
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // 1. Movie Poster
            if (!movie.fullPosterUrl.isNullOrBlank()) {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(movie.fullPosterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

            } else {

                // Poster fallback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Wine,
                                    PrimaryBlueDark
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = RoseGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // 2. Cinematic Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.40f),
                                0.30f to Color.Transparent,
                                0.55f to Wine.copy(alpha = 0.15f),
                                1.0f to Wine.copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // 3. Top Section: Rating + Favourite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Rating Badge
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 5.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MovieRating,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = movie.formattedRating,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Favourite Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    IconButton(
                        onClick = {
                            onFavouriteToggle(movie)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavourite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = if (isFavourite) {
                                "Remove from favourites"
                            } else {
                                "Add to favourites"
                            },
                            tint = heartColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // 4. Bottom Section: Movie Information
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 14.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // Movie Title
                Text(
                    text = movie.title ?: "Untitled",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp
                )

                // Release Year + Language
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {

                    Text(
                        text = movie.releaseYear,
                        color = RoseGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(
                                RoseGold.copy(alpha = 0.8f),
                                CircleShape
                            )
                    )

                    Text(
                        text = (movie.originalLanguage ?: "EN")
                            .uppercase(),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
