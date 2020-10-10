package com.stylenet.android.tv.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Movie
import com.stylenet.android.tv.models.Series

class SeriesHolder(val view: View): RecyclerView.ViewHolder(view) {

    private lateinit var series: Series
    private var textView = itemView.findViewById<TextView>(R.id.text_view)
    private var imageView = itemView.findViewById<ImageView>(R.id.image_view)

    fun bind(series: Series){
        this.series = series
        textView.text = series.title
        if(series.imageLink.isNotEmpty()){
            Picasso.get().load(series.imageLink).into(imageView)
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