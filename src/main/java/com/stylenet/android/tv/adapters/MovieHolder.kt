package com.stylenet.android.tv.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Movie

class MovieHolder(val view: View) : RecyclerView.ViewHolder(view){
    private lateinit var movie: Movie
    private var textView = itemView.findViewById<TextView>(R.id.text_view)
    private var imageView = itemView.findViewById<ImageView>(R.id.image_view)

    fun bind(movie: Movie){
        this.movie = movie
        textView.text = movie.title
        if(movie.imageLink.isNotEmpty()){
            Picasso.get().load(movie.imageLink).into(imageView)
        }
    }


    /*
    * Add the function to add a custom click listener
     */
    fun addClickListener(clickListener: View.OnClickListener){
        view.setOnClickListener(clickListener)
    }


    /*
    * Add the function to add a custom focus listener
     */
    fun addFocusListener(focusChangeListener: View.OnFocusChangeListener){
        view.onFocusChangeListener = focusChangeListener
    }
}
