package com.stylenet.android.tv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Channel


class ChannelAdapter(var channels: ArrayList<Channel>, var callbacks: Callbacks) :
    RecyclerView.Adapter<ChannelHolder>(),
    Filterable
{
    interface Callbacks{
        val customFocusListener: View.OnFocusChangeListener
        fun onChannelClicked(index: Int)
    }

    private val channelsAll = ArrayList<Channel>(channels)
    private val filter = ChannelFilter()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_channel_list_item, parent, false)
        return ChannelHolder(view)
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
        holder.bind(channels[position], position)
        holder.addClickListener(View.OnClickListener { callbacks.onChannelClicked(position) })
        holder.addFocusListener(callbacks.customFocusListener)
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class ChannelFilter: Filter(){
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val filteredList =  ArrayList<Channel>()

            if(charSequence.isNullOrEmpty()){
                filteredList.addAll(channelsAll)
            }
            else{
                for(movie in channelsAll){
                    val contains = movie.name.contains(charSequence, true)
                    if(contains){
                        filteredList.add(movie)
                    }
                }
            }
            return FilterResults().apply {
                values = filteredList
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            this@ChannelAdapter.channels.clear()
            this@ChannelAdapter.channels.addAll(results?.values as ArrayList<Channel>)
            this@ChannelAdapter.notifyDataSetChanged()
        }
    }
}