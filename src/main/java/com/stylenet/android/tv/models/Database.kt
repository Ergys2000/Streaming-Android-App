package com.stylenet.android.tv.models

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.stylenet.android.tv.R
import org.json.JSONObject
import java.lang.Exception
import java.util.HashMap

class Database private constructor(val context: Context) {

    companion object{
        private var database: Database? = null
        fun initialize(context: Context){
            val mSharedPrefs = context.getSharedPreferences(
                context.resources.getString(R.string.shared_prefs_name),
                Context.MODE_PRIVATE)
            database = Database(context).apply {
                this.username = mSharedPrefs.getString(
                    context.resources.getString(R.string.user_key),
                    null)
                this.key = mSharedPrefs.getString(
                    context.resources.getString(R.string.install_key),
                    null)
                this.apiLink = context.resources.getString(R.string.api_link)
            }
        }
        fun get(): Database{
            return database?: throw Exception("The database is not initialized")
        }
    }

    var apiLink = context.resources.getString(R.string.api_link)
    val requestQueue = Volley.newRequestQueue(context)
    var username: String? = null
    var key: String? = null
    var list_nr: Int = 1

    fun authenticate(responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener){
        val requestLink = apiLink + "user_exists.php?username=$username&password=$key"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            requestLink,
            null,
            responseListener, errorListener
        )
        requestQueue.add(jsonObjectRequest)
    }
    fun getChannels(responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener){
        val requestLink = apiLink + "get_channels.php?username=$username&password=$key"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            requestLink,
            null,
            responseListener,
            errorListener
        )
        requestQueue.add(jsonObjectRequest)
    }
    fun updateUserStatus(online: Int, watching: String, lastOnline: String?){
        val requestLink = apiLink + "update_user_status.php?username=$username&password=$key"

        val stringObjectRequest = object: StringRequest(Request.Method.POST, requestLink,
            Response.Listener<String> {
                Log.i("User status updated: ", it.toString())
            },
            Response.ErrorListener {
                Log.e("Error: ", it.toString())
            }
        ){
            override fun getParams(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = username!!
                params["password"] = key!!
                params["online"] = online.toString()
                params["watching"] = watching
                if(lastOnline != null){
                    params["last_online"] = lastOnline
                }
                return params
            }
        }
        requestQueue.add(stringObjectRequest)
    }
    fun getMovies(search: String?, responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener){

        var requestLink = apiLink + "get_movies.php?username=$username&password=$key"
        if(!search.isNullOrEmpty()){
            requestLink += "&search=$search"
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            requestLink,
            null,
            responseListener,
            errorListener
        )
        requestQueue.add(jsonObjectRequest)
    }
    fun getSeries(search: String?, responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener){

        var requestLink = apiLink + "get_series.php?username=$username&password=$key"
        if(!search.isNullOrEmpty()){
            requestLink += "&search=$search"
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            requestLink,
            null,
            responseListener,
            errorListener
        )
        requestQueue.add(jsonObjectRequest)
    }
    fun getEpisodes(title: String, responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener){
        val requestLink = apiLink + "get_episodes.php?username=$username&password=$key&series_name=$title"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            requestLink,
            null,
            responseListener,
            errorListener
        )
        requestQueue.add(jsonObjectRequest)
    }
}