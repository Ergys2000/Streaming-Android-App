package com.stylenet.android.tv.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.stylenet.android.tv.R
import com.stylenet.android.tv.adapters.MovieAdapter
import com.stylenet.android.tv.models.Database
import com.stylenet.android.tv.models.Movie
import org.json.JSONObject

class MovieListFragment: Fragment(), MovieAdapter.Callbacks {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: EditText
    private lateinit var searchButton: Button
    private var movies: ArrayList<Movie> = ArrayList()

    private var callbacks: Callbacks? = null

    private val db = Database.get()

    companion object{
        fun newInstance(): MovieListFragment{
            return MovieListFragment()
        }
    }

    interface Callbacks {
        fun onMovieClicked(link: String, title: String)
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
        getMovies(null)
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

    // for the movieHolder
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

    // for the MovieHOlder
    override fun onMovieClicked(movie: Movie) {
        callbacks?.onMovieClicked(movie.link, movie.title)
    }

    private fun setButtonActions(){
        searchButton.apply {
            setOnClickListener {
                val search = searchView.text.toString()
                getMovies(search)
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
        recyclerView.adapter = MovieAdapter(movies, this as MovieAdapter.Callbacks)
    }

    // methods for accessing the api and getting the list of movies
    private fun getMovies(search: String?){
        db.getMovies(
            search,
            Response.Listener {
                parseJsonResponse(it)
            },
            Response.ErrorListener {
                Log.e("MovieListFragment: ", "Error connecting!")
            }
        )
    }
    private fun parseJsonResponse(jsonResponse: JSONObject){
        if(jsonResponse.getString("status") == "OK"){
            val jsonSeriesList = jsonResponse.getJSONArray("results")
            //iterate through them
            movies.clear()
            for(i in 0 until jsonSeriesList.length()){
                // for each channel
                val channelJson = jsonSeriesList.getJSONObject(i)
                // if its enabled
                if(channelJson.getInt("enabled") == 1){
                    // add it to the list
                    val s = Movie(channelJson.getString("title"), channelJson.getString("link"),
                        channelJson.getString("image_link"))
                    movies.add(s)
                }
            }
            updateUI()
        }
    }
}