package com.stylenet.android.tv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Series

class SeriesAdapter(var series: ArrayList<Series>, var callbacks: Callbacks):
    RecyclerView.Adapter<SeriesHolder>()
{

    interface Callbacks{
        val customFocusListener: View.OnFocusChangeListener
        fun onSeriesClicked(series: Series)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.fragment_movie_list_item,
            parent, false)
        return SeriesHolder(itemView)
    }

    override fun onBindViewHolder(holder: SeriesHolder, position: Int) {
        holder.bind(series[position])
        holder.addClickListener(View.OnClickListener { callbacks.onSeriesClicked(series[position]) })
        holder.addFocusListener(callbacks.customFocusListener)
    }

    override fun getItemCount(): Int {
        return series.size
    }

}
