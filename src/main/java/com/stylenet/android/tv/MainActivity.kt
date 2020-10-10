package com.stylenet.android.tv

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.stylenet.android.tv.fragments.*
import com.stylenet.android.tv.models.Episode


class MainActivity : AppCompatActivity(),
    LogInFragment.Callbacks,
    FrontPageFragment.Callbacks,
    EpisodeListFragment.Callbacks,
    UserConfigFragment.Callbacks,
    MovieListFragment.Callbacks,
    SeriesListFragment.Callbacks{

    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPrefs = this.getSharedPreferences(
            getString(R.string.shared_prefs_name), Context.MODE_PRIVATE)

        val currFragment: Fragment?  =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currFragment == null){
            if(installationConfigured()){
                val fragment = LogInFragment()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
            else{
                val fragment = UserConfigFragment()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onUserAccepted(username: String, password: String) {
        val fragment = FrontPageFragment.getInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onUserRegistered() {
        val fragment = LogInFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onTvClicked() {
        val fragment = MenuChannelViewFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onMoviesClicked() {
        val fragment = MovieListFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    override fun onMovieClicked(link: String, title: String) {
        val fragment = MoviePlayer.newinstance(title, link)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

    }

    override fun onSeriesListClicked() {
        // create the fragment of the series list
        val fragment = SeriesListFragment.newInstance()

        // replace the fragment container with that fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    override fun onSeriesClicked(title: String) {
        val fragment = EpisodeListFragment.newInstance(title)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEpisodeClicked(episode: Episode) {
        val title = "Season " + episode.season + " - " + "Episode " + episode.episode
        val fragment = MoviePlayer.newinstance(title ,episode.link)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currFragment: Fragment?  =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(currFragment is MenuChannelViewFragment){
            when(keyCode){
                KeyEvent.KEYCODE_MENU -> {
                    currFragment.onMenuPressed()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    currFragment.onRightDpadPressed()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    currFragment.onLeftDpadPressed()
                    return true
                }
                KeyEvent.KEYCODE_0 -> {
                    currFragment.onNumPadPressed(0)
                }
                KeyEvent.KEYCODE_1 -> {
                    currFragment.onNumPadPressed(1)
                }
                KeyEvent.KEYCODE_2 -> {
                    currFragment.onNumPadPressed(2)
                }
                KeyEvent.KEYCODE_3 -> {
                    currFragment.onNumPadPressed(3)
                }
                KeyEvent.KEYCODE_4 -> {
                    currFragment.onNumPadPressed(4)
                }
                KeyEvent.KEYCODE_5 -> {
                    currFragment.onNumPadPressed(5)
                }
                KeyEvent.KEYCODE_6 -> {
                    currFragment.onNumPadPressed(6)
                }
                KeyEvent.KEYCODE_7 -> {
                    currFragment.onNumPadPressed(7)
                }
                KeyEvent.KEYCODE_8 -> {
                    currFragment.onNumPadPressed(8)
                }
                KeyEvent.KEYCODE_9 -> {
                    currFragment.onNumPadPressed(9)
                }
            }
        }
        else if(currFragment is MoviePlayer){
            currFragment.playerView.showController()
        }
        else if(currFragment is FrontPageFragment && keyCode == KeyEvent.KEYCODE_BACK){
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    private fun installationConfigured(): Boolean{
        val key = mSharedPrefs.getString(getString(R.string.install_key), null)
        return !key.isNullOrEmpty()
    }


}
