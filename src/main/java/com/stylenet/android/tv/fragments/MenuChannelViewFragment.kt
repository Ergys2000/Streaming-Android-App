package com.stylenet.android.tv.fragments

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.stylenet.android.tv.R
import com.stylenet.android.tv.adapters.ChannelAdapter
import com.stylenet.android.tv.models.Channel
import com.stylenet.android.tv.models.Database
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "TestFragment"
class MenuChannelViewFragment: Fragment(), ChannelAdapter.Callbacks{
    private lateinit var recyclerView: RecyclerView
    private lateinit var numberTextView: TextView
    private lateinit var playerView: PlayerView
    private lateinit var player: SimpleExoPlayer
    private lateinit var menuView: View

    var channels: ArrayList<Channel> = ArrayList()
    var index: Int = 0

    // used to implement the remote number presses
    var currentNumber: Int = 0
    val handler = Handler() // used for the remote numbers


    // to know whether the menu is hidden or not
    private var menuShown = true
    private var db = Database.get()


    companion object{
        fun newInstance(): Fragment{
            val fragment = MenuChannelViewFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu_view, container, false)
        playerView = view.findViewById(R.id.videoView)
        menuView = view.findViewById(R.id.menu)
        recyclerView = view.findViewById(R.id.channel_recycler_view)
        numberTextView = view.findViewById(R.id.number_text_view)
        return view
    }

    // methods to override
    override fun onStart() {
        super.onStart()
        initializePlayer() // initialize the player with the settings
        updateUI() // update the UI with the current channels
        getChannels() // get the channels, which updates the recyclerview adapter
        setListeners()
    }
    override fun onStop() {
        super.onStop()
        releasePlayer()
        val date = getCurrentDateTime()
        val datestring = date.toString("yyyy-MM-dd HH:mm:ss")
        if(channels.size == 0){
            db.updateUserStatus(0, "No channel", datestring)
        }
        else{
            db.updateUserStatus(0, channels[index].name, datestring)
        }
    }

    override val customFocusListener: View.OnFocusChangeListener
        get() {
            return View.OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.channelViewFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }
        }
    override fun onChannelClicked(index: Int) {
        this.index = index
        notifyChannelChanged()
    }

    // utility functions for getting the current date and time
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    // animations
    private fun hideAnimation(){
        val finalValue = menuView.width.toFloat()
        ObjectAnimator
            .ofFloat(menuView, "x", -finalValue)
            .setDuration(700)
            .start()
        menuShown = false
    }
    private fun showAnimation(){
        ObjectAnimator
            .ofFloat(menuView, "x", 0F)
            .setDuration(700)
            .start()
        menuShown = true
    }

    // functions to wire up the buttons
    private fun setListeners(){
        menuView.findViewById<EditText>(R.id.search_view).apply {
            addTextChangedListener(object: TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val adap = recyclerView.adapter as ChannelAdapter
                    adap.filter.filter(p0)
                }

            })
        }
        playerView.setOnClickListener {
            if(menuShown){
                hideAnimation()
            }else{
                showAnimation()
            }
        }
    }

    // setting up the player
    private fun makeCustomLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder().setBufferDurationsMs(
            // when we have less than 40 secs buffered we start buffering again
            40*1000,
            // we stop buffering when we have 1 min ready
            60*1000,
            // we start playing after 0.3 sec is buffered
            300,
            // after rebuffering we make sure we have 1 sec before we start
            1000
        ).createDefaultLoadControl()
    }
    // creates the mediasource for the channel link
    private fun createMediaSource(): MediaSource {
        val url: String = channels[index].link
        val dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(requireContext(), "stylenet")) // we use the default

        // now we check for different types of streams to accomodate the correct media source factory
        return if(url.endsWith(".m3u8", true)){
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(Uri.parse(url))
        }
        else if(url.endsWith(".mpd", true)){
            DashMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        }
        else{
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
        }
    }

    // initializes the player the fragment is first run with the basic settings
    private fun initializePlayer(){
        try{
            val loadControl = makeCustomLoadControl()
            player = SimpleExoPlayer.Builder(requireContext())
                .setLoadControl(loadControl)
                .build()

            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            playerView.player = player
            player.playWhenReady = true
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }

    private fun releasePlayer(){
        playerView.player = null
        player.release()
    }

    // for updating the list of channels
    private fun updateUI(){
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = ChannelAdapter(channels, this as ChannelAdapter.Callbacks)
    }
    private fun notifyChannelChanged(){
        if(channels.size > 0){
            player.prepare(createMediaSource())
            db.updateUserStatus(1, channels[index].name, null)
        }
    }

    // remote commands overrides
    fun onMenuPressed(){
        when(menuShown){
            true -> hideAnimation()
            false -> showAnimation()
        }
    }
    fun onRightDpadPressed(){
        index = (index + 1)%channels.size
        notifyChannelChanged()
        Log.i(TAG, "New Channel Index: $index")
    }
    fun onLeftDpadPressed(){
        index = (index - 1)%channels.size
        if(index < 0){
            index = channels.size -1
        }
        notifyChannelChanged()
        Log.i(TAG, "New Channel Index: $index")
    }
    fun onNumPadPressed(number: Int){
        if (currentNumber == 0){
            currentNumber = number
            handler.postDelayed({
                if(currentNumber > 0 && currentNumber <= channels.size){
                    index = currentNumber - 1
                    notifyChannelChanged()
                    recyclerView.smoothScrollToPosition(index)
                }
                numberTextView.setText("")
                currentNumber = 0
            }, 3000)
        } else {
            currentNumber = currentNumber * 10 + number
        }
        numberTextView.setText(currentNumber.toString())
    }

    // accessing the api to get the channels
    private fun getChannels(){
        db.getChannels(
            Response.Listener{
                parseJsonResponse(it)
            },
            Response.ErrorListener {
                Toast.makeText(context, "There was an error getting the channels!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    private fun parseJsonResponse(jsonResponse: JSONObject){
        if(jsonResponse.getString("status") == "OK"){
            val jsonChannelList = jsonResponse.getJSONArray("results")
            //iterate through them
            channels.clear()
            for(i in 0 until jsonChannelList.length()){
                // for each channel
                val channelJson = jsonChannelList.getJSONObject(i)
                // if its enabled
                if(channelJson.getInt("enabled") == 1){
                    // add it to the list
                    val channel = Channel(channelJson.getString("title"), channelJson.getString("link"))
                    channels.add(channel)
                }
            }
            recyclerView.adapter = ChannelAdapter(channels, this as ChannelAdapter.Callbacks)
            index = 0
            notifyChannelChanged()
        }
        else
        {
            Toast.makeText(context, jsonResponse.getString("error"), Toast.LENGTH_SHORT).show()
        }
    }

    fun onChannelListChanged(){
        getChannels()
    }
}