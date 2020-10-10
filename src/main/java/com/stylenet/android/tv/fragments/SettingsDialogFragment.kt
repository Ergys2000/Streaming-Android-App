package com.stylenet.android.tv.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.core.view.size
import androidx.fragment.app.DialogFragment
import com.stylenet.android.tv.R
import java.util.*

class SettingsDialogFragment: DialogFragment() {
    private var callbacks: Callbacks? = null
    private var isChecked: Boolean = false
    private var currentSpeed: Float = 1F
    private lateinit var switch: Switch
    private lateinit var speedSpinner: Spinner

    companion object{
        fun newInstance(isChecked: Boolean, currentSpeed: Float): SettingsDialogFragment{
            val dialogFragment = SettingsDialogFragment().apply {
                this.isChecked = isChecked
                this.currentSpeed = currentSpeed
            }
            return dialogFragment
        }
    }

    // the parent fragment will implement this and will take care of the methods defined here
    interface Callbacks{
        fun onVolumeSwitched()
        fun onSpeedChanged(value: Float)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflates the view and initializes the settings values
        val view = inflater.inflate(R.layout.dialog_fragment_settings,
            container, false)
        switch = view.findViewById(R.id.volume_switch)
        switch.isChecked = isChecked

        speedSpinner = view.findViewById(R.id.speed)
        populateSpinner()

        setActions()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = parentFragment as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun populateSpinner(){
        // creates the arrayAdapter for the spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.speedOptions, // the array holding the options
            android.R.layout.simple_spinner_item // the layout that defines the spinner
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            speedSpinner.adapter = arrayAdapter
        }
        // set the selection to the current  value of the speed
        speedSpinner.setSelection(getIndex(currentSpeed))
    }

    private fun getIndex(speed: Float): Int {
        return (speed / 0.25).toInt() - 1 // since we have speeds from 0.25 to 2, we use this formula
    }

    // here we set the actions that each one of our widgets performs
    private fun setActions(){
        switch.setOnClickListener {
            callbacks?.onVolumeSwitched() // call the parent fragment defined method
        }
        speedSpinner.apply{
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val value = parent?.getItemAtPosition(position).toString()
                    callbacks?.onSpeedChanged(value.toFloat())
                }
            }
        }
    }
}