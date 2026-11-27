
package com.example.exam.feature.api.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.exam.data.remote.model.MovieDto
import com.example.exam.data.remote.model.MovieDetailsDto
import com.example.exam.databinding.FragmentDeviceDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentDeviceDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApiViewModel by activityViewModels()

    private var currentMovie: MovieDto? = null
    private var currentFavouriteIds: Set<Int> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceDetailBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupFavouriteButton()
        observeMovieDetails()
    }

    private fun setupBackButton() {
        binding.btnDetailBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupFavouriteButton() {
        binding.btnDetailFavourite.setOnClickListener {

            val movie = currentMovie ?: return@setOnClickListener

            viewModel.toggleFavourite(movie)
        }
    }

    private fun observeMovieDetails() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    currentFavouriteIds = state.favouriteIds

                    val movie = state.selectedMovieDetail

                    if (movie != null) {

                        currentMovie = MovieDto(
                            id = movie.id,
                            title = movie.title,
                            originalTitle = movie.title,
                            overview = movie.overview,
                            posterPath = movie.posterPath,
                            backdropPath = movie.backdropPath,
                            releaseDate = movie.releaseDate,
                            voteAverage = movie.voteAverage,
                            voteCount = movie.voteCount,
                            popularity = movie.popularity,
                            originalLanguage = movie.originalLanguage
                        )

                        displayMovie(movie)
                        updateFavouriteButton(movie.id)
                    }
                }
            }
        }
    }

    private fun displayMovie(movie: MovieDetailsDto) {

        binding.tvDetailTitle.text = movie.title.orEmpty()

        binding.tvDetailYear.text =
            movie.releaseDate?.take(4) ?: "Release date unavailable"

        binding.tvDetailRating.text = String.format(
            Locale.US,
            "★ %.1f / 10",
            movie.voteAverage ?: 0.0
        )

        binding.tvDetailLanguage.text =
            "Language: ${movie.originalLanguage ?: "Unknown"}"

        binding.tvDetailOverview.text =
            movie.overview?.takeIf { it.isNotBlank() }
                ?: "No overview available."

        binding.tvDetailOriginalTitle.text =
            "Original title: ${movie.title.orEmpty()}"

        binding.tvDetailVoteCount.text =
            "Votes: ${movie.voteCount ?: 0}"

        binding.ivDetailPoster.load(movie.fullPosterUrl) {
            crossfade(true)
            placeholder(android.R.color.darker_gray)
            error(android.R.color.darker_gray)
        }

        binding.ivDetailBackdrop.load(movie.fullBackdropUrl) {
            crossfade(true)
            placeholder(android.R.color.darker_gray)
            error(android.R.color.darker_gray)
        }
    }

    private fun updateFavouriteButton(movieId: Int) {

        val isFavourite = movieId in currentFavouriteIds

        binding.btnDetailFavourite.text =
            if (isFavourite) {
                "♥  Added to Favourites"
            } else {
                "♡  Add to Favourites"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
