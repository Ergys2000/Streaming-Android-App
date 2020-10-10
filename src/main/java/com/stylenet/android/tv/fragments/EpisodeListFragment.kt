package com.stylenet.android.tv.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Database
import com.stylenet.android.tv.models.Episode
import org.json.JSONObject

private const val ListKey: String = "title"
class EpisodeListFragment: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EpisodeAdapter
    private var episodes: ArrayList<Episode> = ArrayList()
    private var callbacks: Callbacks? = null

    private lateinit var title: String

    private val db = Database.get()

    // every time we get an instance of this class we use this function
    // even though right now it does nothing later we can add stuff
    companion object{
        fun newInstance(title: String): Fragment{
            val bundle = Bundle()
            bundle.apply {
                putString(ListKey, title)
            }
            return EpisodeListFragment().apply {
                arguments = bundle
            }
        }
    }
    // define an interface the main activity will implement
    interface Callbacks{
        fun onEpisodeClicked(episodes: Episode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(ListKey) as String
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_episode_list, container, false)
        recyclerView = view.findViewById(R.id.channel_recycler_view) as RecyclerView
        return view
    }

    // this updates the list that the user sees.
    private fun updateUI(){
        // sets the layoutManager of the recycler view to a linear layout manager
        recyclerView.layoutManager = LinearLayoutManager(context)
        // sets the adapter of the recycler view to our defined adapter
        adapter = EpisodeAdapter(episodes)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        updateUI()
        getEpisodes()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private inner class EpisodeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var channel: Episode
        private var index: Int = 0
        private var nameTextView: TextView = itemView.findViewById(R.id.title)

        fun bind(channel: Episode, i: Int){
            this.channel = channel
            this.index = i
            nameTextView.setText("Season ${channel.season} - Episode ${channel.episode}")
        }

        init{
            itemView.setOnClickListener(this)
            itemView.onFocusChangeListener = customFocusListener()
        }

        // we also implemented the interface onClickListener so we can hook up our functions to
        // our view holders right here
        override fun onClick(v: View?) {
            callbacks?.onEpisodeClicked(episodes[index])
        }

        private inner class customFocusListener(): View.OnFocusChangeListener{
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.channelViewFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }

        }
    }

    private inner class EpisodeAdapter(var episodes: ArrayList<Episode>) :
        RecyclerView.Adapter<EpisodeHolder>()
    {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_episode_item, parent, false)
            return EpisodeHolder(view)
        }

        override fun getItemCount(): Int {
            return episodes.size
        }

        // defines how we bind the holder at that position, apparently we assign it the proper
        // channel
        override fun onBindViewHolder(holder: EpisodeHolder, position: Int) {
            val channel = episodes[position]
            holder.bind(channel, position)
        }

    }

    private fun getEpisodes(){

        db.getEpisodes(title,
            Response.Listener{
                parseJsonResponse(it)
            },
            Response.ErrorListener {
                Toast.makeText(context, "Error getting the episodes!", Toast.LENGTH_LONG).show()
            }
        )

    }
    private fun parseJsonResponse(jsonResponse: JSONObject){
        if(jsonResponse.getString("status") == "OK"){
            val jsonSeriesList = jsonResponse.getJSONArray("results")
            //iterate through them
            episodes.clear()
            for(i in 0 until jsonSeriesList.length()){
                // for each channel
                val episodeJson = jsonSeriesList.getJSONObject(i)

                val s = Episode(episodeJson.getInt("season"), episodeJson.getInt("episode"),
                    episodeJson.getString("link"))
                episodes.add(s)
            }
            updateUI()
        }
    }
}