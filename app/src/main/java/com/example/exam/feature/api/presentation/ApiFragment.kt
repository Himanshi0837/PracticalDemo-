
package com.example.exam.feature.api.presentation
import androidx.navigation.fragment.findNavController
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.exam.R
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.widget.NestedScrollView
import com.example.exam.databinding.FragmentApiBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context

@AndroidEntryPoint
class ApiFragment : Fragment() {

    private var _binding: FragmentApiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApiViewModel by activityViewModels()

    private lateinit var movieAdapter: MovieGridAdapter

    private var isUpdatingSearch = false
    private var isUpdatingChips = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupCategories()
        setupRetryButton()
        setupPagination()
        observeUiState()
    }


    private fun setupRecyclerView() {

        movieAdapter = MovieGridAdapter(
            onMovieClick = { movie ->

                val navController = findNavController()

                // Prevent multiple detail screens
                if (navController.currentDestination?.id == R.id.apiFragment) {

                    viewModel.openMovieDetail(movie.id)

                    navController.navigate(R.id.deviceDetailFragment)
                }
            },
            onFavouriteClick = { movie ->
                viewModel.toggleFavourite(movie)
            }
        )

        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = movieAdapter
            setHasFixedSize(true)
        }
    }


    private fun setupSearch() {

        binding.etMovieSearch.doAfterTextChanged { editable ->

            if (!isUpdatingSearch) {
                viewModel.onSearchQueryChange(editable?.toString().orEmpty())
            }
        }

        binding.ivClearSearch.setOnClickListener {
            binding.etMovieSearch.text?.clear()
        }
        binding.etMovieSearch.setOnEditorActionListener { view, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {

                // Hide keyboard
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.hideSoftInputFromWindow(view.windowToken, 0)

                // Scroll to top
                binding.scrollMovies.smoothScrollTo(0, 0)

                true
            } else {
                false
            }
        }
    }

    private fun setupCategories() {

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->

            if (isUpdatingChips) return@setOnCheckedStateChangeListener

            val selectedChip = group.findViewById<Chip>(
                checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            )

            val category = when (selectedChip.id) {
                binding.chipAll.id -> MovieCategory.TRENDING
                binding.chipAction.id -> MovieCategory.POPULAR
                binding.chipComedy.id -> MovieCategory.TOP_RATED
                binding.chipDrama.id -> MovieCategory.UPCOMING
                binding.chipRomance.id -> MovieCategory.FAVOURITES
                else -> MovieCategory.TRENDING
            }

            viewModel.selectCategory(category)
        }
    }

    private fun setupRetryButton() {

        binding.btnRetryMovies.setOnClickListener {
            viewModel.retry()
        }
    }
    private fun setupPagination() {

        binding.scrollMovies.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { scrollView, _, scrollY, _, _ ->

                val contentView = scrollView.getChildAt(0)
                    ?: return@OnScrollChangeListener

                val threshold =
                    (300 * resources.displayMetrics.density).toInt()

                val reachedBottom =
                    scrollY + scrollView.height >=
                            contentView.measuredHeight - threshold

                if (reachedBottom) {
                    viewModel.loadNextPage()
                }
            }
        )
    }

    private fun observeUiState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->

                    movieAdapter.submitMovies(state.movies)
                    movieAdapter.updateFavourites(state.favouriteIds)

                    // Status message
                    binding.tvApiStatus.text = when {
                        state.isLoading -> "Loading movies..."
                        state.errorMessage != null -> state.errorMessage
                        else -> "Movies loaded: ${state.movies.size}"
                    }
                    binding.loadingOverlay.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE


                    // Pagination loading
                    binding.progressPagination.visibility =
                        if (state.isPaginationLoading) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    // Empty state
                    binding.tvEmptyMovies.visibility =
                        if (!state.isLoading &&
                            state.movies.isEmpty() &&
                            state.errorMessage == null
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    // Retry button
                    binding.btnRetryMovies.visibility =
                        if (state.errorMessage != null) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    // Movie list
                    binding.rvMovies.visibility =
                        if (state.movies.isEmpty()) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                    // Search text
                    if (binding.etMovieSearch.text.toString() != state.searchQuery) {
                        isUpdatingSearch = true

                        binding.etMovieSearch.setText(state.searchQuery)

                        binding.etMovieSearch.setSelection(
                            binding.etMovieSearch.text?.length ?: 0
                        )

                        isUpdatingSearch = false
                    }

                    updateSelectedCategory(state.selectedCategory)
                }
            }
        }
    }

    private fun updateSelectedCategory(category: MovieCategory) {

        val selectedChipId = when (category) {
            MovieCategory.TRENDING -> binding.chipAll.id
            MovieCategory.POPULAR -> binding.chipAction.id
            MovieCategory.TOP_RATED -> binding.chipComedy.id
            MovieCategory.UPCOMING -> binding.chipDrama.id
            MovieCategory.FAVOURITES -> binding.chipRomance.id
        }

        if (binding.chipGroupCategories.checkedChipId != selectedChipId) {

            isUpdatingChips = true

            binding.chipGroupCategories.check(selectedChipId)

            isUpdatingChips = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding.rvMovies.adapter = null
        _binding = null
    }
}
