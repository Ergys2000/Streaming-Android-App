package com.stylenet.android.tv.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Database
import java.util.*

class FrontPageFragment: Fragment() {
    private lateinit var tvButton: Button
    private lateinit var youtubeButton: Button
    private lateinit var moviesButton: Button
    private lateinit var seriesButton: Button
    private lateinit var exitButton: Button

    private var database: Database = Database.get()
    private lateinit var requestLink: String
    private lateinit var jsonObjectRequest: JsonObjectRequest

    private var keepChecking: Boolean = true

    private var callbacks: Callbacks? = null
    private var activity: AppCompatActivity? = null


    interface Callbacks{
        fun onTvClicked()
        fun onMoviesClicked()
        fun onSeriesListClicked()
    }

    companion object{
        fun getInstance(): Fragment{
            val fragment = FrontPageFragment()
            return fragment
        }
    }

    // overriding lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init_req()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_front_page, container, false)
        tvButton = view.findViewById(R.id.tv)
        youtubeButton = view.findViewById(R.id.youtube)
        moviesButton = view.findViewById(R.id.movies)
        seriesButton = view.findViewById(R.id.series)
        exitButton = view.findViewById(R.id.exit)
        return view
    }

    override fun onResume() {
        super.onResume()
        context?.cacheDir?.deleteRecursively()
    }
    override fun onStart() {
        super.onStart()
        setButtonActions()
        createUserListener()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks // when the fragment is attached we initialize callbacks
        activity = context as AppCompatActivity
    }
    override fun onDetach() {
        super.onDetach()
        callbacks = null // when the fragment is detached we set the callbacks to null
    }

    // initialize the request
    private fun init_req(){
        requestLink = database.apiLink + "user_exists.php?username=${database.username}&password=${database.key}"

        jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, requestLink, null,
            Response.Listener{
                val enabled = it.getJSONObject("results").getBoolean("exists")
                val list_nr = it.getJSONObject("results").getInt("list_nr")

                if(enabled){
                    if(list_nr != database.list_nr){
                        database.list_nr = list_nr
                        updateChannels()
                    }
                }else{
                    keepChecking = false
                    returnHomeScreen()
                }
            },
            Response.ErrorListener {
            }
        )
    }
    // wiring up the buttons
    private fun setButtonActions(){
        youtubeButton.apply{
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/"))
                startActivity(intent)
            }
        }
        tvButton.apply{
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                callbacks?.onTvClicked()
            }
        }
        moviesButton.apply{
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                callbacks?.onMoviesClicked()
            }
        }
        seriesButton.apply{
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                callbacks?.onSeriesListClicked()
            }
        }
        exitButton.apply{
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                requireActivity().finish()
/*
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory("android.intent.category.MONKEY")
                startActivity(intent)
*/
            }
        }
    }

    // creating the listener to see when the user is disabled by the admin
    private fun createUserListener(){
        val timer = Timer("User checking loop")
        timer.scheduleAtFixedRate(
            object : TimerTask() {

                override fun run() {
                    if(keepChecking){
                        authenticate()
                    }else{
                        timer.cancel()
                        timer.purge()
                    }
                }

            },0, 2000)
    }
    private fun authenticate(){
        database.requestQueue.add(jsonObjectRequest)
    }

    // functions for kicking a user out or updating the channels
    private fun returnHomeScreen(){
        val fManager = activity!!.supportFragmentManager
        val logInFragment = LogInFragment()

        for(i in 0 .. fManager.backStackEntryCount){
            fManager.popBackStack()
        }
        fManager
            .beginTransaction()
            .replace(R.id.fragment_container, logInFragment)
            .commit()
    }
    private fun updateChannels(){
        val fManager = activity!!.supportFragmentManager
        val currFragment = fManager.findFragmentById(R.id.fragment_container)
        if(currFragment is MenuChannelViewFragment){
            currFragment.onChannelListChanged()
        }

    }
}