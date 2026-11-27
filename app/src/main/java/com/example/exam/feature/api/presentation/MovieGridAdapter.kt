
package com.example.exam.feature.api.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exam.R
import com.example.exam.data.remote.model.MovieDto
import com.google.android.material.card.MaterialCardView
import coil.load

class MovieGridAdapter(
    private val onMovieClick: (MovieDto) -> Unit,
    private val onFavouriteClick: (MovieDto) -> Unit
) : RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder>() {

    private val movies = mutableListOf<MovieDto>()
    private var favouriteIds: Set<Int> = emptySet()

    inner class MovieViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val ivPoster: ImageView =
            itemView.findViewById(R.id.iv_poster)

        private val ivFavourite: ImageView =
            itemView.findViewById(R.id.iv_favourite)

        private val tvRating: TextView =
            itemView.findViewById(R.id.tv_movie_rating)

        private val tvTitle: TextView =
            itemView.findViewById(R.id.tv_movie_title)

        private val tvRelease: TextView =
            itemView.findViewById(R.id.tv_movie_release)

        private val movieCard: MaterialCardView =
            itemView.findViewById(R.id.movie_card)

        fun bind(movie: MovieDto) {

            tvTitle.text = movie.title ?: "Unknown Movie"
            tvRelease.text = movie.releaseYear
            tvRating.text = "★ ${movie.formattedRating}"

            ivPoster.load(movie.fullPosterUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_search_box)
                error(R.drawable.bg_search_box)
            }

            val isFavourite = favouriteIds.contains(movie.id)

            ivFavourite.setImageResource(
                if (isFavourite) {
                    android.R.drawable.btn_star_big_on
                } else {
                    android.R.drawable.btn_star_big_off
                }
            )

            ivFavourite.setOnClickListener {
                onFavouriteClick(movie)
            }

            movieCard.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MovieViewHolder,
        position: Int
    ) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    fun submitMovies(newMovies: List<MovieDto>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun updateFavourites(newFavouriteIds: Set<Int>) {
        favouriteIds = newFavouriteIds
        notifyDataSetChanged()
    }
}
