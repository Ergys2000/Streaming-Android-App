package com.stylenet.android.tv.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Database

class LogInFragment: Fragment() {
    private lateinit var logInButton: Button
    private lateinit var exitButton: Button
    private lateinit var progressBar: ProgressBar

    private var callbacks: Callbacks? = null

    private lateinit var database: Database

    // the callback interface that the mainActivity extends to implement functions
    interface Callbacks{
        fun onUserAccepted(username: String, password: String)
    }

    // overriding lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Database.initialize(requireContext())
        database = Database.get()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        logInButton = view.findViewById(R.id.log_in)
        exitButton = view.findViewById(R.id.exit)
        progressBar = view.findViewById(R.id.progress_circular)
        checkSharedPrefs() // this needs to be here because the fragment needs to be attached

        return view
    }
    override fun onStart() {
        super.onStart()
        setButtonActions()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    // wiring up the buttons
    private fun setButtonActions(){
        logInButton.apply{
            setOnFocusChangeListener{ v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
            setOnClickListener {
                val install_key: String? = database.key
                val username = database.username
                if(install_key != null && username != null){
                    authenticate(username, install_key)
                }
            }
        }
        exitButton.apply{
            setOnClickListener {
                val fragment = ExitDialogFragment()
                fragment.show(childFragmentManager, "ExitDialog")
                //requireActivity().finish()
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

    // checking the shared preferences to see if this installation is registered
    private fun checkSharedPrefs(){
        val install_key: String? = database.key
        val username = database.username
        if(username != null && install_key != null){
            authenticate(username, install_key)
        }
    }

    // authenticating with the server
    private fun authenticate(username: String, key: String){
        progressBar.visibility = View.VISIBLE
        database.authenticate(
            Response.Listener{

                if(it.getJSONObject("results").getBoolean("exists")){
                    onLogInSuccess(username, key)
                }else{
                    Toast.makeText(context, "User disabled, or does not belong to this installation!"
                        , Toast.LENGTH_SHORT).show()
                }
                progressBar.visibility = View.INVISIBLE
            },
            Response.ErrorListener {
                Toast.makeText(context, "Internet connection failed!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.INVISIBLE
            }
        )
    }

    private fun onLogInSuccess(username: String, password: String){
        val mainActivity = requireActivity()
        val currFragment: Fragment?  =
            mainActivity.supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currFragment is LogInFragment){
            callbacks?.onUserAccepted(username, password)
        }
    }

}