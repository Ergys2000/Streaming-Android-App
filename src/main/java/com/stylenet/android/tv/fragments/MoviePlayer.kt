package com.stylenet.android.tv.fragments
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import java.lang.Exception

class MoviePlayer: Fragment(), SettingsDialogFragment.Callbacks{
    private var url: String? = null
    private var title: String? = null
    public lateinit var playerView: PlayerView // the playerView displayed
    private lateinit var player: SimpleExoPlayer // the player used to play the video

    companion object{
        fun newinstance(title: String, link: String): Fragment{
            return MoviePlayer().apply {
                this.url = link
                this.title = title
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_view,
            container, false)
        playerView = view.findViewById(R.id.videoView)
        return view
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
        setButtonActions()
    }
    override fun onStop(){
        super.onStop()
        releasePlayer()
    }

    // creates the media source for the current url
    private fun createMediaSource(): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(requireContext(), "stylenet"))
        return if(url!!.endsWith(".m3u8", true)){
            HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
        } else if(url!!.endsWith(".mpd", true)){
            DashMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        } else{
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
        }
    }

    private fun releasePlayer(){
        playerView.player = null
        player.release()
    }

    private fun initializePlayer(){
        try{
            val loadControl = makeCustomLoadControl()
            player = SimpleExoPlayer.Builder(requireContext())
                .setLoadControl(loadControl)
                .build()
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            playerView.player = player
            player.prepare(createMediaSource())
            player.playWhenReady = true
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }catch(e: Exception){
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun makeCustomLoadControl(): DefaultLoadControl{
        return DefaultLoadControl.Builder().setBufferDurationsMs(
            // when we have less than 8 mins buffered we start buffering again
            8*60*1000,
            // we stop buffering when we have 10 mins ready
            10*60*1000,
            // we start playing after 1 sec is buffered
            1*1000,
            // after rebuffering we make sure we have 3 sec before we start
            3*1000
        ).createDefaultLoadControl()
    }

    private fun setButtonActions(){
        playerView.findViewById<ImageButton>(R.id.settings).apply {
            setOnClickListener {
                val isChecked = player.volume == 0F
                val speed = player.playbackParameters.speed

                val fragmentManager = childFragmentManager
                val fragment = SettingsDialogFragment.newInstance(isChecked, speed)
                //val fragment = ChannelListDialogFragment()
                fragment.show(fragmentManager, "dialog")
            }
            setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }
        }
        playerView.findViewById<ImageButton>(R.id.back).apply{
            setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }
        }
        playerView.findViewById<TextView>(R.id.title).text = title
    }

    override fun onVolumeSwitched() {
        val currVolume = player.volume
        if(currVolume > 0F){
            player.volume = 0F
        }else{
            player.volume = 1F
        }
    }

    override fun onSpeedChanged(value: Float) {
        val playbackParameters = PlaybackParameters(value)
        player.setPlaybackParameters(playbackParameters)
    }
}
