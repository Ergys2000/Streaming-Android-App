package com.stylenet.android.tv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Movie

class MovieAdapter(var movies: ArrayList<Movie>, var callbacks: Callbacks):
    RecyclerView.Adapter<MovieHolder>()
{

    interface Callbacks{
        val customFocusListener: View.OnFocusChangeListener
        fun onMovieClicked(movie: Movie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.fragment_movie_list_item,
            parent, false)
        return MovieHolder(itemView)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.bind(movies[position])
        holder.addClickListener(View.OnClickListener { callbacks.onMovieClicked(movies[position]) })
        holder.addFocusListener(callbacks.customFocusListener)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

}
