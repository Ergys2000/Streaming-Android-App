package com.stylenet.android.tv.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stylenet.android.tv.R
import com.stylenet.android.tv.adapters.SeriesAdapter
import com.stylenet.android.tv.models.Database
import com.stylenet.android.tv.models.Series
import org.json.JSONObject

class SeriesListFragment: Fragment(), SeriesAdapter.Callbacks {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: EditText
    private lateinit var searchButton: Button
    private var series: ArrayList<Series> = ArrayList()

    private var callbacks: Callbacks? = null

    private val db = Database.get()

    companion object{
        fun newInstance(): SeriesListFragment{
            return SeriesListFragment()
        }
    }

    interface Callbacks {
        fun onSeriesClicked(title: String)
    }

    override val customFocusListener: View.OnFocusChangeListener
        get() = View.OnFocusChangeListener { view, hasFocus ->
            val textContainer = view?.findViewById<RelativeLayout>(R.id.title_container)
            val textView = view?.findViewById<TextView>(R.id.text_view)
            if(hasFocus){
                textContainer?.setBackgroundColor(resources.getColor(R.color.black))
                textView?.setTextColor(resources.getColor(R.color.white))
            }else{
                textContainer?.setBackgroundColor(resources.getColor(R.color.white))
                textView?.setTextColor(resources.getColor(R.color.black))
            }

        }

    override fun onSeriesClicked(series: Series) {
        callbacks?.onSeriesClicked(series.title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_list, container, false)
        recyclerView = view.findViewById(R.id.movie_recycler_view)
        searchView = view.findViewById(R.id.search_view)
        searchButton = view.findViewById(R.id.search)
        return view
    }

    override fun onStart() {
        super.onStart()
        updateUI()
        getSeries(null)
        setButtonActions()
    }

    override fun onDetach() {
        super.onDetach()
        this.callbacks = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.callbacks = context as Callbacks
    }

    private fun setButtonActions(){
        searchButton.apply{
            setOnClickListener {
                val search = searchView.text.toString()
                getSeries(search)
            }
            setOnFocusChangeListener{ v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
        }
    }

    private fun updateUI(){
        recyclerView.layoutManager = GridLayoutManager(this.requireContext(), 3)
        recyclerView.adapter = SeriesAdapter(series, this as SeriesAdapter.Callbacks)
    }

    // methods for accessing the api and getting the list of series
    private fun getSeries(search: String?){
        db.getSeries(search,
            Response.Listener{
                parseJsonResponse(it)
            },
            Response.ErrorListener {
                Toast.makeText(context, "There was an error getting the series.", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun parseJsonResponse(jsonResponse: JSONObject){
        if(jsonResponse.getString("status") == "OK"){
            val jsonSeriesList = jsonResponse.getJSONArray("results")
            //iterate through them
            series.clear()
            for(i in 0 until jsonSeriesList.length()){
                // for each channel
                val channelJson = jsonSeriesList.getJSONObject(i)
                // if its enabled
                if(channelJson.getInt("enabled") == 1){
                    // add it to the list
                    val s = Series(channelJson.getString("title"), channelJson.getString("image_link"))
                    series.add(s)
                }
            }
            updateUI()
        }
    }
}