package com.stylenet.android.tv.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Channel

class ChannelHolder(val view: View): RecyclerView.ViewHolder(view) {
    private lateinit var channel: Channel
    private var index: Int = 0
    private var nameTextView: TextView = itemView.findViewById(R.id.title)

    fun bind(channel: Channel, i: Int){
        this.channel = channel
        this.index = i
        nameTextView.text = "%s . %s".format((index+1), channel.name)
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