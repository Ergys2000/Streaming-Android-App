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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stylenet.android.tv.R
import org.json.JSONObject
import java.util.*

class UserConfigFragment: Fragment() {
    private lateinit var usernameField: EditText
    private lateinit var keyField: EditText
    private lateinit var adminKeyField: EditText
    private lateinit var nameField: EditText
    private lateinit var surnameField: EditText
    private lateinit var addressField: EditText
    private lateinit var registerBtn: Button

    private lateinit var apiLink: String
    private lateinit var requestQueue: RequestQueue

    private lateinit var mSharedPrefs: SharedPreferences
    private var callbacks: Callbacks? = null

    interface Callbacks{
        fun onUserRegistered()
    }

    // overriding lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPrefs = requireActivity().getSharedPreferences(getString(R.string.shared_prefs_name),
        Context.MODE_PRIVATE)
        apiLink = getString(R.string.api_link)
        requestQueue = Volley.newRequestQueue(this.requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configure_user, container, false)
        usernameField = view.findViewById(R.id.username)
        keyField = view.findViewById(R.id.install_key)
        adminKeyField = view.findViewById(R.id.admin_key)
        nameField = view.findViewById(R.id.name)
        surnameField = view.findViewById(R.id.surname)
        addressField = view.findViewById(R.id.address)

        registerBtn = view.findViewById(R.id.register)
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
        registerBtn.apply {
            setOnClickListener{
                register()
            }
            setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus){
                    view?.setBackgroundColor(resources.getColor(R.color.colorAccent))
                }else{
                    view?.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
        }
        keyField.setText(UUID.randomUUID().toString())
    }

    // functions for operations with the database
    private fun register(){
        val username = usernameField.text.toString()
        val key = keyField.text.toString()
        val name = nameField.text.toString()
        val surname = surnameField.text.toString()
        val address = addressField.text.toString()

        val adminKey = adminKeyField.text.toString()
        if(username.isNotEmpty() && key.isNotEmpty() && adminKey.isNotEmpty()){
            writeToDatabase(username, key, name, surname, address, adminKey)
        }else{
            Toast.makeText(context, "Please fill in the username and password", Toast.LENGTH_SHORT).show();
        }
    }

    private fun writeToDatabase(username: String, key: String, name: String, surname: String,
                                address: String, adminKey: String){
        val requestLink = apiLink + "add_user.php"
        val stringRequest = object: StringRequest(
            Method.POST, requestLink,
            Response.Listener{
                parseStringResponse(it, username, key)
            },
            Response.ErrorListener {
                Log.e("Error: ", it.toString());
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = username
                params["password"] = key
                params["name"] = name
                params["surname"] = surname
                params["address"] = address
                params["admin_key"] = adminKey
                return params
            }
        }
        requestQueue.add(stringRequest)
    }
    private fun parseStringResponse(stringResponse: String, username: String, key: String){
        val jsonObject = JSONObject(stringResponse)
        if(jsonObject.getString("status") == "OK"){
            writeToSharedPrefs(username, key)
            callbacks?.onUserRegistered()
        }else{
            Toast.makeText(context, jsonObject.getString("error"),
                Toast.LENGTH_SHORT).show()
        }

    }
    private fun writeToSharedPrefs(username: String, key: String){
        with(mSharedPrefs.edit()){
            putString(getString(R.string.user_key), username)
            putString(getString(R.string.install_key), key)
            commit()
        }
        Log.i("Info: ", "Shared preferences updated!")
    }
}